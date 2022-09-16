package main

import (
	"encoding/json"
	"fmt"
	rtctokenbuilder "github.com/AgoraIO/Tools/DynamicKey/AgoraDynamicKey/go/src/RtcTokenBuilder"
	rtmtokenbuilder "github.com/AgoraIO/Tools/DynamicKey/AgoraDynamicKey/go/src/RtmTokenBuilder"
	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"log"
	"math/rand"
	"os"
	"strconv"
	"time"
)

type Token struct {
	UID    uint32 `json:"uid"`
	RTMUID string `json:"rtmuid"`
	RTC    string `json:"rtc"`
	RTM    string `json:"rtm"`
}

func generateRTMToken(uid string) string {
	appID := os.Getenv("APP_ID")
	appCertificate := os.Getenv("CERTIFICATE")
	expireTimeInSeconds := uint32(24 * 60 * 60)
	currentTimestamp := uint32(time.Now().UTC().Unix())
	expireTimestamp := currentTimestamp + expireTimeInSeconds

	result, err := rtmtokenbuilder.BuildToken(appID, appCertificate, uid, rtmtokenbuilder.RoleRtmUser, expireTimestamp)

	if err != nil {
		log.Printf("Error %+v", err)
	}
	return result
}

func generateRtcToken(uid uint32, channelName string, role rtctokenbuilder.Role) string {
	appID := os.Getenv("APP_ID")
	appCertificate := os.Getenv("CERTIFICATE")
	tokenExpireTimeInSeconds := uint32(60 * 60 * 24)
	result, err := rtctokenbuilder.BuildTokenWithUID(appID, appCertificate, channelName, uid, role, tokenExpireTimeInSeconds)
	if err != nil {
		log.Printf("Error %+v", err)
	}
	return result
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
	rtmToken := generateRTMToken(rtmUid)

	rtcToken := generateRtcToken(uid, rtcChannel, rtctokenbuilder.RolePublisher)

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
