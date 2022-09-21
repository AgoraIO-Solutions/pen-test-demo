//
//  NetworkClient.swift
//  Pen Test Demo
//
//  Created by shaun on 9/19/22.
//

import Foundation
import OSLog

struct AesKey: Codable {
    let key: String
}

struct Tokens: Codable {
    let uid: UInt
    let rtmuid: String
    let rtc: String
    let rtm: String
}

private func baseURLComponents(path: String) throws -> URLComponents {
    let apiBase: String = try Configuration.value(for: "AWS_API_BASE")
    let url = URL(string: "https://" + apiBase + path)!

    return URLComponents(url: url, resolvingAgainstBaseURL: false)!
}

private enum NetworkRequest {
    case aesKey(String)
    case tokens(String)

    func getUrlRequest() throws -> URLRequest {
        let apiKey: String = try Configuration.value(for: "AWS_APP_KEY")

        let path: String
        let channelName: String
        switch self {
        case let.aesKey(channel):
            path = "/dev/api/pen_test_aes_key"
            channelName = channel
        case let.tokens(channel):
            path = "/dev/api/pen_test_token"
            channelName = channel
        }

        var urlComponents = try baseURLComponents(path: path)
        urlComponents.queryItems = [
            URLQueryItem(name: "channel", value: channelName)
        ]


        guard let url = urlComponents.url else { fatalError("url is required") }

        var urlRequest = URLRequest(url: url)
        urlRequest.setValue(apiKey, forHTTPHeaderField: "X-API-KEY")

        return urlRequest
    }
}


struct NetworkClient {
    static let decoder = JSONDecoder()

    static let logger = Logger(subsystem: SubsystemIdentifier, category: "Network Client")

    static func getAesKey(channelName: String) async throws -> AesKey {
        return try await getFromNetwork(networkRequest: .aesKey(channelName))
    }

    static func getToken(channelName: String) async throws -> Tokens {
        return try await getFromNetwork(networkRequest: .tokens(channelName))
    }

    private static func getFromNetwork<T: Decodable>(networkRequest: NetworkRequest) async throws -> T {
        let urlReq = try networkRequest.getUrlRequest()
        let (data, resp) = try await URLSession.shared.fetchData(urlReq)
        if let httpResponse = (resp as? HTTPURLResponse) {
            logger.info("http response \(httpResponse)")
        }

        return try decoder.decode(T.self, from: data)
    }
}

enum URLSessionError: Error {
    case missingResponseOrData
}

extension URLSession {
    func fetchData(_ urlRequest: URLRequest) async throws -> (Data, URLResponse) {
        return try await withCheckedThrowingContinuation { continuation in
            URLSession.shared.dataTask(with: urlRequest) { maybeData, maybeResp, maybeErr in
                if let err = maybeErr {
                    return continuation.resume(throwing: err)
                }

                guard let resp = maybeResp, let data = maybeData else {
                    return continuation.resume(throwing: URLSessionError.missingResponseOrData)
                }

                continuation.resume(returning: (data, resp) )
            }.resume()
        }
    }
}
