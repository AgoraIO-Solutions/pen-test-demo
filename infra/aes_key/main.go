package main

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/awserr"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
	"github.com/aws/aws-sdk-go/service/kms"
	"log"
)

type aekKey struct {
	KEY string `json:"key"`
}

const tableName = "ChannelAesKeys"
const primaryKey = "channel"
const aesAttributeKey = "aes"

func storeKey(newKey string, channelName string, dbSession *dynamodb.DynamoDB) (*dynamodb.PutItemOutput, error) {
	// store a key
	log.Println("creating Put item input")
	input := &dynamodb.PutItemInput{
		Item: map[string]*dynamodb.AttributeValue{
			primaryKey: {
				S: aws.String(channelName),
			},
			aesAttributeKey: {
				S: aws.String(newKey),
			},
		},
		TableName: aws.String(tableName),
	}
	log.Println("putting ", input)
	output, err := dbSession.PutItem(input)
	if err != nil {
		log.Println("err: Error", err.Error())
		if aerr, ok := err.(awserr.Error); ok {
			log.Println("aeRR: Error", aerr)
		}
	}
	return output, err
}

func generateAKey(channnel string) (*string, error) {
	sess, err := session.NewSession(&aws.Config{
		Region: aws.String("us-west-1")},
	)

	svc := kms.New(sess)
	keyId := "arn:aws:kms:us-west-1:307306839941:key/38abf113-5b68-4c18-97d1-923f8193c5b6"

	input := &kms.GenerateDataKeyInput{
		KeyId:   aws.String(keyId),
		KeySpec: aws.String("AES_256"),
	}

	result, err := svc.GenerateDataKey(input)
	if err != nil {
		return nil, err
	}

	text := base64.URLEncoding.EncodeToString(result.Plaintext)
	text32 := text[:32]

	return &text32, nil
}

func getDbSession() (*dynamodb.DynamoDB, error) {
	cfg := aws.NewConfig().WithRegion("us-west-1")
	sess, err := session.NewSession(cfg)
	if err != nil {
		return nil, err
	}
	return dynamodb.New(sess), nil
}

func getChannelKeyIfExists(dbSession *dynamodb.DynamoDB, channelName string) (*string, error) {
	// TODO: IF Shankara/Yaniv say we need to be more resillient then a few hours, we should timebox keys to whatever the resilence is with expiration
	// or more securely provision an HMS and use rotation based on time which will raise client complexity as client side AES keys will require rotation
	// this should work or demo purposes

	// Find aes Key if it exists
	input := &dynamodb.GetItemInput{
		TableName: aws.String(tableName),
		Key: map[string]*dynamodb.AttributeValue{
			primaryKey: {
				S: aws.String(channelName),
			},
		},
	}
	result, err := dbSession.GetItem(input)
	if err != nil {
		if aerr, ok := err.(awserr.Error); ok {
			switch aerr.Code() {
			case dynamodb.ErrCodeResourceNotFoundException:
				return nil, nil
			default:
				log.Println(aerr.Error())
				return nil, aerr
			}
		}
	}

	attributes := result.Item
	if attributes == nil || attributes[aesAttributeKey] == nil {
		return nil, nil
	}
	return attributes[aesAttributeKey].S, nil
}

func generate500ErrResponse(err error) (events.APIGatewayProxyResponse, error) {
	return events.APIGatewayProxyResponse{
		Body:       err.Error(),
		StatusCode: 500,
	}, nil
}

func generate400ErrResponse(err error) (events.APIGatewayProxyResponse, error) {
	return events.APIGatewayProxyResponse{
		Body:       err.Error(),
		StatusCode: 400,
	}, nil
}

func generateKeyResponse(aes string) (events.APIGatewayProxyResponse, error) {
	key := aekKey{KEY: aes}

	keyJson, _ := json.Marshal(key)

	return events.APIGatewayProxyResponse{
		Body:       string(keyJson),
		StatusCode: 200,
	}, nil
}

func getToken(event events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	channelName := event.QueryStringParameters["channel"]
	if len(channelName) == 0 {
		return generate400ErrResponse(errors.New("error: Error channel is a required query parameter"))
	}
	dbSession, sesErr := getDbSession()
	if sesErr != nil {
		return generate500ErrResponse(sesErr)
	}

	// Check if aes key exists in store
	maybeKey, channelLookupErr := getChannelKeyIfExists(dbSession, channelName)
	if channelLookupErr != nil {
		return generate500ErrResponse(channelLookupErr)
	}

	log.Println("checked for a key")
	if maybeKey != nil {
		log.Println("got a key")
		return generateKeyResponse(*maybeKey)
	} else {
		// generate a Key
		newKey, genKeyErr := generateAKey(channelName)
		if genKeyErr != nil {
			return generate500ErrResponse(genKeyErr)
		}
		log.Println("generated a key")
		_, storeErr := storeKey(*newKey, channelName, dbSession)
		if storeErr != nil {
			return generate500ErrResponse(storeErr)
		}

		log.Println("stored a key")
		return generateKeyResponse(*newKey)
	}
}

func main() {
	lambda.Start(getToken)
}
