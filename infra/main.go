package main

import (
	"encoding/json"
	"fmt"
	rtctokenbuilder "github.com/AgoraIO/Tools/DynamicKey/AgoraDynamicKey/go/src/RtcTokenBuilder"
	rtmtokenbuilder "github.com/AgoraIO/Tools/DynamicKey/AgoraDynamicKey/go/src/RtmTokenBuilder"
	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-secretsmanager-caching-go/secretcache"
	"log"
	"math/rand"
	"strconv"
	"time"
)

var (
	secretCache, _ = secretcache.New()
)

func GetAWSSecret(secretId string) (string, error) {
	result, err := secretCache.GetSecretString("Meta_Pen_Test_Prod_Secrets")
	if err != nil {
		return "", err
	}
	secretMap := map[string]string{}
	json.Unmarshal([]byte(result), &secretMap)

	return secretMap[secretId], nil
}

type Token struct {
	UID    uint32 `json:"uid"`
	RTMUID string `json:"rtmuid"`
	RTC    string `json:"rtc"`
	RTM    string `json:"rtm"`
}

func generateRTMToken(uid string) (string, error) {
	appID, idErr := GetAWSSecret("APP_IDs")
	if idErr != nil {
		return "", idErr
	}
	appCertificate, certErr := GetAWSSecret("CERTIFICATE")
	if certErr != nil {
		return "", certErr
	}
	expireTimeInSeconds := uint32(24 * 60 * 60)
	currentTimestamp := uint32(time.Now().UTC().Unix())
	expireTimestamp := currentTimestamp + expireTimeInSeconds

	result, err := rtmtokenbuilder.BuildToken(appID, appCertificate, uid, rtmtokenbuilder.RoleRtmUser, expireTimestamp)

	if err != nil {
		log.Printf("Error %+v", err)
	}
	return result, nil
}

func generateRtcToken(uid uint32, channelName string, role rtctokenbuilder.Role) (string, error) {
	appID, idErr := GetAWSSecret("APP_IDs")
	if idErr != nil {
		return "", idErr
	}
	appCertificate, certErr := GetAWSSecret("CERTIFICATE")
	if certErr != nil {
		return "", certErr
	}
	tokenExpireTimeInSeconds := uint32(60 * 60 * 24)
	result, err := rtctokenbuilder.BuildTokenWithUID(appID, appCertificate, channelName, uid, role, tokenExpireTimeInSeconds)
	if err != nil {
		log.Printf("Error %+v", err)
	}
	return result, nil
}

func generateARandomUID() uint32 {
	rand.Seed(time.Now().UnixNano())
	return rand.Uint32()
}

func getToken(event events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Println("Got an event ", event)
	uid := generateARandomUID()
	rtcChannel := event.QueryStringParameters["Channel"]

	rtmUid := strconv.FormatUint(uint64(uid), 10)
	rtmToken, rtmErr := generateRTMToken(rtmUid)
	if rtmErr != nil {
		return events.APIGatewayProxyResponse{
			Body:       rtmErr.Error(),
			StatusCode: 500,
		}, nil
	}

	rtcToken, rtcErr := generateRtcToken(uid, rtcChannel, rtctokenbuilder.RolePublisher)
	if rtmErr != nil {
		return events.APIGatewayProxyResponse{
			Body:       rtcErr.Error(),
			StatusCode: 500,
		}, nil
	}

	token := Token{
		UID: uid, RTC: rtcToken, RTM: rtmToken, RTMUID: rtmUid,
	}
	tokenText, _ := json.Marshal(token)

	return events.APIGatewayProxyResponse{
		Body:       string(tokenText),
		StatusCode: 200,
	}, nil
}

func main() {
	lambda.Start(getToken)
}
