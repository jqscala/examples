package commits_fs2

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import jq._, jqfs2.given

@main def main(): Unit =
    scala.util.Properties.envOrNone("BEARER_GITHUB_TOKEN").fold{
        println("Environment variable BEARER_GITHUB_TOKEN is not set")
        System.exit(1)
    }{ token =>
        given LoggerFactory[IO] = Slf4jFactory.create[IO]
        val repo: String = "https://api.github.com/repos/jserranohidalgo/urjc-pd"
        
        val commits: List[Json | TypeError[Json]] = 
            Commits.from[IO](repo, token)
                //.take(2)
                .through(iterator | arr(i"author.login", i"commit.message", i"commit.author.date"))
                .run
        
        commits.foreach(println)
    }