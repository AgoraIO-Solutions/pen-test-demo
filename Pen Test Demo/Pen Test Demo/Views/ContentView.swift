//
//  ContentView.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var rteManager: RTEManager

    var body: some View {
        NavigationView {
            if rteManager.loggedIn {
                LoggedInView()

            } else {
                LoginView()
                    .navigationTitle("Welcome")
            }
        }.navigationViewStyle(.stack)
            .navigationBarHidden(true)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
