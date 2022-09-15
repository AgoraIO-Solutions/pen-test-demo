//
//  TextView.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import SwiftUI


struct TextView: View {
    @EnvironmentObject private var rtmManager: RTMManager

    @State private var text = ""


    var body: some View {
        VStack {
            List(rtmManager.messages, id: \.self) {
                Text($0).padding()
            }
            TextField("Say Something", text: $text)
                .onSubmit {
                    let sentText = text
                    Task {
                        await rtmManager.send(sentText)
                    }
                    text = ""
                }
                .padding()

        }
        .adaptsToKeyboard()
        .navigationTitle(Text("Send Messages"))
    }
}

struct TextView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            TextView().environmentObject(RTEManager())
        }
    }
}

import Combine

struct AdaptsToKeyboard: ViewModifier {
    @State var currentHeight: CGFloat = 0

    func body(content: Content) -> some View {
        GeometryReader { geometry in
            content
                .padding(.bottom, self.currentHeight)
                .onAppear(perform: {
                    NotificationCenter.Publisher(center: NotificationCenter.default, name: UIResponder.keyboardWillShowNotification)
                        .merge(with: NotificationCenter.Publisher(center: NotificationCenter.default, name: UIResponder.keyboardWillChangeFrameNotification))
                        .compactMap { notification in
                            withAnimation(.easeOut(duration: 0.16)) {
                                notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect
                            }
                    }
                    .map { rect in
                        rect.height - geometry.safeAreaInsets.bottom
                    }
                    .subscribe(Subscribers.Assign(object: self, keyPath: \.currentHeight))

                    NotificationCenter.Publisher(center: NotificationCenter.default, name: UIResponder.keyboardWillHideNotification)
                        .compactMap { notification in
                            CGFloat.zero
                    }
                    .subscribe(Subscribers.Assign(object: self, keyPath: \.currentHeight))
                })
        }
    }
}

extension View {
    func adaptsToKeyboard() -> some View {
        return modifier(AdaptsToKeyboard())
    }
}
