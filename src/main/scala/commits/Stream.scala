package commits 

import cats.effect.Async
import cats.effect.IO
import fs2.Pipe
import fs2.Pull
import fs2.Stream
import io.circe.Json
import io.circe.jawn.CirceSupportParser
import io.circe.optics.JsonPath._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{`export` => _, _}
import org.http4s.Method
import org.http4s.Request
import org.http4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.typelevel.jawn.Facade
import org.typelevel.jawn.fs2._
import org.typelevel.log4cats.LoggerFactory
import cats.effect.unsafe.IORuntime

extension [A](st: Stream[IO, A])
    def run(using IORuntime): List[A] = 
        st.compile.toList.unsafeRunSync()

extension (S: Stream.type)

    def fromUri[F[_]: cats.effect.kernel.Async: LoggerFactory](u: org.http4s.Uri, token: String): Stream[F, Json] = 
        given Facade[Json] = new CirceSupportParser(None, false).facade
        val authHeader =  Authorization(Credentials.Token(AuthScheme.Bearer, token))
        Stream.resource(EmberClientBuilder.default[F].build)
            .flatMap: client =>
                client.stream(Request[F](Method.GET, u).withHeaders(authHeader))
                    .flatMap(_.body.chunks.parseJsonStream)