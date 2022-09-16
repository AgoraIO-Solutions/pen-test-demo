//
//  RTCView.swift
//  Pen Test Demo
//
//  Created by shaun on 9/14/22.
//

import SwiftUI

struct RTCView: View {
    @EnvironmentObject var rtcManager: RTCManager

    var body: some View {
        NavigationView {
            GeometryReader { proxy in
                let heightOfMainVideo = proxy.size.height * 0.5
                let heightOfControls = proxy.size.height * 0.2
                let heightOfNonPrimaryVideos = proxy.size.height * 0.3

                VStack(alignment: .center) {
                    focusedView
                        .frame(maxWidth: .infinity, maxHeight: heightOfMainVideo, alignment: .center)


                    ScrollView(.horizontal) {
                        LazyHStack(alignment: .center, spacing: 10) {
                            ForEach(rtcManager.sortedRtcUsers) { rtcUser in
                                RTCDetailView(rtcUser: rtcUser, fullSize: false)
                                    .frame(width: heightOfNonPrimaryVideos, height: heightOfNonPrimaryVideos)
                                    .onTapGesture {
                                        rtcManager.focusedRtcUser = rtcUser
                                    }
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: heightOfNonPrimaryVideos, alignment: .center)

                    VStack {
                        HStack {
                            Spacer()
                            Button {
                                rtcManager.publishAudio.toggle()
                            } label: {
                                if rtcManager.publishAudio {
                                    Image(systemName: "mic")
                                } else {
                                    Image(systemName: "mic.slash")
                                }
                            }

                            Spacer()

                            Button {
                                rtcManager.publishVideo.toggle()
                            } label: {
                                if rtcManager.publishVideo {
                                    Image(systemName: "video")
                                } else {
                                    Image(systemName: "video.slash")
                                }
                            }

                            Spacer()
                        }
                        List {
                            Picker("Video Resolution", selection: $rtcManager.videoQuality) {
                                Text("High").tag(VideoQuality.high)
                                Text("Medium").tag(VideoQuality.medium)
                                Text("Low").tag(VideoQuality.low)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: heightOfControls, alignment: .center)

                }
            }
            .navigationBarHidden(true)
        }.navigationViewStyle(.stack)
        .navigationBarHidden(true)
    }


    @ViewBuilder
    private var focusedView: some View {
        if rtcManager.focusedRtcUser.uid != 0 {
            RTCDetailView(rtcUser: rtcManager.focusedRtcUser, fullSize: true)
        } else {
            Image(systemName: "camera")
        }
    }
}

struct RTCView_Previews: PreviewProvider {
    static var previews: some View {
        RTCView().environmentObject(RTEManager())

    }
}
