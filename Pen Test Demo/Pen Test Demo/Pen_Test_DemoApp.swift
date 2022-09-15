//
//  Pen_Test_DemoApp.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import SwiftUI

@main
struct Pen_Test_DemoApp: App {
    @StateObject private var rteManager = RTEManager()


    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(rteManager)
                .environmentObject(rteManager.rtmManager)
        }
    }
}
