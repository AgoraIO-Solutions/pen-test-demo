//
//  LoggedInView.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import SwiftUI

struct LoggedInView: View {
    var body: some View {
        TabView {
            RTCView().tabItem {
                Text("RTC")
                Image(systemName: "video.fill")
            }
            TextView().tabItem {
                Text("RTM")
                Image(systemName: "text.bubble")
            }
            SettingsScreen().tabItem {
                Text("Settings")
                Image(systemName: "gear.circle")
            }
        }
    }
}

struct LoggedInView_Previews: PreviewProvider {
    static var previews: some View {
        LoggedInView()
    }
}
