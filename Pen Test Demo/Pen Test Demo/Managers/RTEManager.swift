//
//  RTEManager.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import Foundation
import OSLog
import Combine

class RTEManager: NSObject, ObservableObject {
    private let logger = Logger(subsystem: "Managers", category: "RTE")

    @Published private(set) var rtmManager: RTMManager
    private var rtmConnectionStateSub: AnyCancellable?

    private let userId = UInt32.random(in: 0..<(1_000_000))

    override init() {
        guard let rtmId: String = try? Configuration.value(for: "APP_ID") else { fatalError("must have app id in config") }
        self.rtmManager = RTMManager(appId: rtmId, userId: "\(userId)")
        super.init()
        rtmConnectionStateSub = rtmManager.connectionState
            .receive(on: RunLoop.main)
            .subscribe(on: RunLoop.main)
            .sink { connectionState in
                self.loggedIn = connectionState == .connected
        }


    }

    @Published var loggedIn: Bool = false

}


