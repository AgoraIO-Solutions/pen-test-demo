//
//  RTCManager.swift
//  Pen Test Demo
//
//  Created by shaun on 9/15/22.
//

import Foundation
import OSLog
import SwiftUI
import AgoraRtcKit
import Combine


enum VideoQuality {
    case low, medium, high
}


class RTCUser: ObservableObject, Hashable, Identifiable {
    static func == (lhs: RTCUser, rhs: RTCUser) -> Bool {
        return lhs.uid == rhs.uid
    }

    @Published var fps = 15 
    let uid: UInt

    init(uid: UInt) {
        self.uid = uid
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(uid)
    }
}


class RTCManager: NSObject, ObservableObject {
    private let logger = Logger(subsystem: SubsystemIdentifier, category: "RTCManager")
    private(set) var engine: AgoraRtcEngineKit!
    private var users: [UInt: RTCUser] = [:] {
        didSet {
            self.objectWillChange.send()
        }
    }
    var connectionState = CurrentValueSubject<ConnectionState, Never>(ConnectionState.disconnected)

    @Published var networkQuality = "Loading..."
    @Published var myUid: UInt = 0 {
        didSet {
            objectWillChange.send()
        }
    }

    @Published var videoQuality = VideoQuality.medium {
        didSet {
            adjustVideoQuality()
        }
    }


    @Published var focusedRtcUser = RTCUser(uid: 0)

    @Published var publishAudio = false {
        didSet {
            guard engine != .none else { return }
            engine.muteLocalAudioStream(!publishAudio)
        }
    }
    @Published var publishVideo = true {
        didSet {
            guard engine != .none else { return }
            engine.muteLocalVideoStream(!publishVideo)
        }
    }

    var sortedRtcUsers: [RTCUser] {
        return users
            .keys.filter { $0 != focusedRtcUser.uid }
            .sorted()
            .compactMap { users[$0] } // consistently get the same order of uids
    }

    init(appId: String) {
        super.init()

        let config = AgoraRtcEngineConfig()
        config.channelProfile = .communication
        config.appId = appId

        engine = .sharedEngine(with: config, delegate: self)
        engine.enableDualStreamMode(true)
        engine.enableAudio()
        engine.enableVideo()
        engine.startPreview()

        engine.muteLocalAudioStream(!publishAudio)
        engine.muteLocalVideoStream(!publishVideo)

        let testStr = "RFG8dpjywFaKZ2JQCzxvD3GbphUCurLC"
        let agoraEncryptionConfig = AgoraEncryptionConfig()
        agoraEncryptionConfig.encryptionKey = testStr
        agoraEncryptionConfig.encryptionMode = .AES256GCM
        engine.enableEncryption(true, encryptionConfig: agoraEncryptionConfig)

        startLastMileProbe()
        adjustVideoQuality()
    }

    func startLastMileProbe() {
        let config = AgoraLastmileProbeConfig()
        config.expectedDownlinkBitrate = 100_000
        config.expectedUplinkBitrate = 100_000
        config.probeUplink = true
        config.probeDownlink = true
        engine.startLastmileProbeTest(config)
    }

    func adjustVideoQuality() {
        let config: AgoraVideoEncoderConfiguration

        switch videoQuality {
        case .low:
            config = .init(
                size: .init(width: 160, height: 120),
                frameRate: .fps15,
                bitrate: AgoraVideoBitrateStandard,
                orientationMode: .adaptative,
                mirrorMode: .auto
            )
        case .medium:
            config = .init(
                size: .init(width: 480, height: 360),
                frameRate: .fps30,
                bitrate: AgoraVideoBitrateStandard,
                orientationMode: .adaptative,
                mirrorMode: .auto
            )
        case .high:
            config = .init(
                size: .init(width: 960, height: 720),
                frameRate: .fps30,
                bitrate: AgoraVideoBitrateStandard,
                orientationMode: .adaptative,
                mirrorMode: .auto
            )
        }

        engine.setVideoEncoderConfiguration(config)
    }
}

// MARK: - Public API
extension RTCManager {
    func joinChannel(name: String) {
        connectionState.send(.connecting)

        let options = AgoraRtcChannelMediaOptions()
        options.channelProfile = .communication
        //options.clientRoleType = .broadcaster


        let status = engine.joinChannel(byToken: .none, channelId: name, uid: myUid, mediaOptions: options) { [weak self] _, uid, _ in
            self?.logger.info("Join success called, joined as \(uid)")
            self?.myUid = uid
            self?.users[uid] = RTCUser(uid: uid)
        }

        if status != 0 {
            connectionState.send(.disconnected)
            logger.error("Error joining \(status)")
        } else {
            connectionState.send(.connected)
        }
    }

    func leave() {
        connectionState.send(.disconnected)
        engine.leaveChannel { [weak self] _ in
            self?.startLastMileProbe()
        }
    }
}

// MARK: - Canvas management
extension RTCManager {
    func setupCanvas(_ uiView: UIView, uid: UInt, fullSize: Bool) {
        engine.setRemoteVideoStream(uid, type: fullSize ? .high : .low)

        if uid == myUid {
            setupCanvasForLocal(uiView, uid: uid)
        } else {
            setupCanvasForRemote(uiView, uid: uid)
        }
    }

    private func setupCanvasForRemote(_ uiView: UIView, uid: UInt) {
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = uid
        canvas.renderMode = .hidden
        canvas.view = uiView
        engine.setupRemoteVideo(canvas)
    }

    private func setupCanvasForLocal(_ uiView: UIView, uid: UInt) {
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = uid
        canvas.renderMode = .hidden
        canvas.view = uiView
        engine.setupLocalVideo(canvas)
    }
}

// MARK: - RTC Delegate
extension RTCManager: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, lastmileQuality quality: AgoraNetworkQuality) {
        networkQuality = quality.description
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        logger.error("Error \(errorCode.rawValue)")
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        logger.warning("Warning \(warningCode.rawValue)")
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        logger.info("Joined \(channel) as uid \(uid)")
        myUid = uid
        users[uid] = RTCUser(uid: uid)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        logger.info("other user joined as \(uid)")
        users[uid] = RTCUser(uid: uid)
        if focusedRtcUser.uid == 0 {
            print("focus should change to \(uid)")
            focusedRtcUser = users[uid]!
        }
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        logger.info("other user left with \(uid)")
        users[uid] = nil
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        users[stats.uid]?.fps = stats.decoderOutputFrameRate
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats, sourceType: AgoraVideoSourceType) {
        users[myUid]?.fps = stats.encoderOutputFrameRate
    }
}


extension AgoraNetworkQuality: CustomStringConvertible {
    public var description: String {
        switch self {
        case .excellent:
            return "Excellent"
        case .good:
            return "Good"
        case .bad:
            return "Bad"
        case .vBad:
            return "Very Bad"
        case .down:
            return "Down"
        case .poor:
            return "Poor"
        default:
            return "Unknown"
        }
    }
}
