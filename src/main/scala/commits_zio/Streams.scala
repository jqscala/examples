package commits_zio

import zio._
import zio.http._
import zio.stream._


extension (S: ZStream.type)
    def fromUri(uri: String, token: String) = 
        Client.streamingWith(
                Request.get(uri)
                    .addHeader(Header.Authorization.Bearer(token))
        )(ZStream.succeed)
        