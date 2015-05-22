package com.finagle.annotated.router

import ConsoleUtils.colored
import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{Path, Root}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpMethod
import Route.getFrom

class RouterBuilder[T]( f: (T => Service[Request, Response]), routes: T*) extends Log {

  def create = {
    createRoutingService(
      routes.map { route =>
        asMatcher(getFrom(route.getClass), f(route))
      } reduceLeft (_ orElse _)
    )
  }

  private def createRoutingService(routes: PartialFunction[(HttpMethod, Path), Service[Request, Response]]) =
    new RoutingService(
      new PartialFunction[Request, Service[Request, Response]] {
        def apply(request: Request) = new Service[Request, Response] {
          override def apply(request: Request): Future[Response] = routes(request.method -> Path(request.path))(request)
        }

        def isDefinedAt(request: Request) = routes.isDefinedAt(request.method -> Path(request.path))
      })

  private def asMatcher(route: Route, service: Service[Request, Response]) = {
    new PartialFunction[(HttpMethod, Path), Service[Request, Response]] {
      override def isDefinedAt(x: (HttpMethod, Path)): Boolean = x._2 == route.path && route.methods.contains(x._1)

      override def apply(v1: (HttpMethod, Path)): Service[Request, Response] = service
    }
  }

  def print() = {
    val methods = routes.map { r =>
      val info = getFrom(r.getClass)
      s"${colored(info.methods.mkString(",").toString, Console.GREEN)}"
    }

    val paths = routes.map { r =>
      val info = getFrom(r.getClass)
      s"${colored(if(info.path == Root) "/" else info.path.toString, Console.WHITE)}"
    }

    val classes = routes.map { r =>
      s"${colored(r.getClass.getSimpleName, Console.YELLOW)}"
    }

    val logs = (methods zip paths) map (x => s" ${x._1} ~ ${x._2}") zip classes map (x => s"${x._1} => ${x._2}")
    log.info("\n\nRoutes: \n\n" + leftPad(logs).mkString("\n") + "\n")
  }

  private def leftPad(list: Seq[String]) = {
    val max = list.map(_.length).max
    list.sortBy(-_.length).map(s => s.padTo(max, " ").mkString)
  }
}
