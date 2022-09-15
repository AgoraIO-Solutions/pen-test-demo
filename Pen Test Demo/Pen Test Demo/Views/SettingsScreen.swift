//
//  SettingsScreen.swift
//  Pen Test Demo
//
//  Created by shaun on 9/15/22.
//

import SwiftUI

struct SettingsScreen: View {
    @EnvironmentObject private var rteManager: RTEManager
    var body: some View {
        Button("Logout") {
            rteManager.leave()
        }
    }
}

struct SettingsScreen_Previews: PreviewProvider {
    static var previews: some View {
        SettingsScreen()
    }
}
