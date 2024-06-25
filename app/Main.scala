import play.core.server.{ProdServerStart, RealServerProcess}

object Main {
  def main(args: Array[String]): Unit = {
    ProdServerStart.start(new RealServerProcess(Seq()))
  }
}
