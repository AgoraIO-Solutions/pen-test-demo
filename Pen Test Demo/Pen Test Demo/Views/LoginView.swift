//
//  LoginView.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import SwiftUI

struct LoginView: View {
    @EnvironmentObject private var rteManager: RTEManager
    @EnvironmentObject private var rtcManager: RTCManager
    @State private var channelName = "test"
    @State private var errorText = false

    var body: some View {
        Form {
            Section(header: Text("Join A Channel")) {
                TextField("Channel Name", text: $channelName)
                    .onChange(of: channelName) { _ in
                        channelName = channelName.trimmingCharacters(in: CharacterSet.alphanumerics.inverted).uppercased()
                        errorText = false
                }
                if errorText {
                    Text("Error, please enter a channel name")
                        .foregroundColor(.red)
                }
            }

            Button("Join", action: join)
                .disabled(rteManager.loggingIn)

            Section(header: Text("Nextwork Quality")) {
                Text("Current Quality \(rtcManager.networkQuality)")
            }
        }
    }

    private func join() {
        if channelName.isBlank {
            errorText = true
        } else {
            rteManager.joinChannel(channelName)
        }
    }
}

struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}


private extension String {
    var isBlank: Bool {
        return self.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
