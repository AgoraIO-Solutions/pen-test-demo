//
//  RTEManager.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import Foundation
import OSLog
import Combine

enum ConnectionState {
    case disconnected, connected, connecting
}

class RTEManager: NSObject, ObservableObject {
    private let logger = Logger(subsystem: SubsystemIdentifier, category: "RTE")

    @Published private(set) var rtmManager: RTMManager
    @Published private(set) var rtcManager: RTCManager
    private var connectionStateSub: AnyCancellable?
    private lazy var connectionPublisher = {
        Publishers.CombineLatest(rtmManager.connectionState, rtcManager.connectionState)
    }()


    override init() {
        guard let rtmId: String = try? Configuration.value(for: "APP_ID") else { fatalError("must have app id in config") }
        self.rtmManager = RTMManager(appId: rtmId)
        self.rtcManager = RTCManager(appId: rtmId)
        super.init()

        connectionStateSub = connectionPublisher
            .receive(on: RunLoop.main)
            .subscribe(on: RunLoop.main)
            .sink { (rtmState, rtcState) in
                guard rtmState == rtcState else {
                    self.loggedIn = false
                    return
                }
                self.loggedIn = rtcState == .connected
        }
    }

    @Published var loggedIn: Bool = false
    @Published var loggingIn: Bool = false

    func joinChannel(_ name: String) {
        guard rtcManager.connectionState.value == .disconnected && rtmManager.connectionState.value == .disconnected else {
            return logger.debug("Debounced join spam")
        }


        Task {
            await MainActor.run {
                loggingIn = true
            }
            do {
                async let aesKeyTask = try NetworkClient.getAesKey(channelName: name)
                async let tokenTask = try NetworkClient.getToken(channelName: name)
                let (tokens, aesKey) = try await (tokenTask, aesKeyTask)
                let (_, _) = await (rtmManager.joinChannel(name, tokens: tokens), rtcManager.joinChannel(name: name, aesKey: aesKey.key, tokens: tokens))
            } catch {
                logger.error("Error joining channel \(error.localizedDescription)")
            }

            await MainActor.run {
                loggingIn = false
            }
        }

    }

    func leave() {
        Task {
            await rtcManager.leave()
            await rtmManager.leaveChannel()
        }
    }
}
