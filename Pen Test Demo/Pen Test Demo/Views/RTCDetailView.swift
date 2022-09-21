//
//  RTCDetailView.swift
//  Pen Test Demo
//
//  Created by shaun on 9/15/22.
//

import SwiftUI

struct VideoView: UIViewRepresentable {
    @EnvironmentObject private var rtcManager: RTCManager
    let uid: UInt
    let highQuality: Bool

    typealias UIViewType = UIView

    func makeUIView(context: Context) -> UIView {
        let view  = UIView()
        // NOTE: @Shankara, if you see this... we are a hop skip and a jump away from having something decently reusable for SwiftUI... NOT a UIKit but rather a Gist that can be reused for for future clients may be worth throwing a sprint at.. at some point if we hve a backlog
        rtcManager.setupCanvas(view, uid: uid, fullSize: highQuality)
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // noop
    }
}

struct RTCDetailView: View {
    @ObservedObject var rtcUser: RTCUser
    let fullSize: Bool

    var body: some View {
        GeometryReader { proxy in

            ZStack(alignment: .topTrailing) {
                VideoView(uid: rtcUser.uid, highQuality: fullSize)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                GeometryReader {
                    Color.black
                        .padding()
                        .clipShape(RoundedRectangle(cornerRadius: $0.size.height / 2))
                        .overlay(VStack {
                            Text("FPS: \(rtcUser.fps)")
                                .foregroundColor(.white)
                        })

                }
                .frame(width: 100, height: 50)
            }.frame(width: proxy.size.height, height: proxy.size.height)
        }
        .id(rtcUser.uid)
    }
}


struct RTCDetailView_Previews: PreviewProvider {
    static var previews: some View {
        HStack {
            let size: CGFloat = 350
            RTCDetailView(rtcUser: RTCUser(uid: 5), fullSize: true).frame(width: size, height: size, alignment: .center)
        }
    }
}

