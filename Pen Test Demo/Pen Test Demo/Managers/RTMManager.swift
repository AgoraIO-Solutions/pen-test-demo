//
//  RTMManager.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import Foundation
import AgoraRtmKit
import OSLog
import Combine

private let logger = Logger(subsystem: "io.agora.PenTestDemo", category: "RTM")

enum ConnectionState {
    case disconnected, connected, connecting
}

class RTMManager: NSObject, ObservableObject {
    private let jsonEncoder = JSONEncoder()
    private let jsonDecoder = JSONDecoder()

    private var rtmKit: AgoraRtmKit?
    private var rtmChannel: AgoraRtmChannel?


    @Published var messages = [String]()
    var connectionState = CurrentValueSubject<ConnectionState, Never>(ConnectionState.disconnected)

    private let agoraAppId: String
    private let userId: String


    init(appId: String, userId: String) {
        agoraAppId = appId
        self.userId = userId
        super.init()

        rtmKit = .init(appId: agoraAppId, delegate: self)
    }

    func joinChannel(_ channelName: String) async {
        // TODO: Require tokens
        let errorCode = await rtmKit?.login(byToken: agoraAppId, user: userId)
        guard errorCode == .ok else {
            logger.error("Error logging into rtm \(errorCode?.rawValue ?? .min)")
            return
        }
        rtmChannel = rtmKit?.createChannel(withId: channelName, delegate: self)
        connectionState.send(.connecting)
        let chanErrorCode = await rtmChannel?.join()
        guard chanErrorCode == .channelErrorOk else { return logger.error("Error joining channel \(chanErrorCode?.rawValue ?? .min)") }
        connectionState.send(.connected)
        await resetMessages()
    }

    func leaveChannel() async {
        let errCode = await rtmChannel?.leave()
        guard errCode == .ok else { return logger.error("Error leaving channel") }
        connectionState.send(.disconnected)
    }

    func send(_ text: String) async {
        let result = await rtmChannel?.send(.init(text: text))
        guard result == .errorOk else { return logger.error("Error sending \(text)") }
        await MainActor.run {
            messages.append(text)
        }
        logger.debug("Sent \(text) successfully")
    }

    private func resetMessages() async {
        await MainActor.run {
            messages = []
        }
    }
}

extension RTMManager: AgoraRtmDelegate {}

extension RTMManager: AgoraRtmChannelDelegate {
    func channel(_ channel: AgoraRtmChannel, messageReceived message: AgoraRtmMessage, from member: AgoraRtmMember) {
        messages.append(message.text)
    }
}
