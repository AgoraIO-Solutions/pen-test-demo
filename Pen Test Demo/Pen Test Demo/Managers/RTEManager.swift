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

    func joinChannel(_ name: String) {
        guard rtcManager.connectionState.value == .disconnected && rtmManager.connectionState.value == .disconnected else {
            return logger.debug("Debounced join spam")
        }

        Task {
            await rtmManager.joinChannel(name)
        }
        rtcManager.joinChannel(name: name)
    }

    func leave() {
        rtcManager.leave()
        Task {
            await rtmManager.leaveChannel()
        }
    }
}
