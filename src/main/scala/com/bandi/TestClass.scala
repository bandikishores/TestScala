package com.bandi

object TestClass {

  case class Employee(var name: String, var invokedCount: Int = 0) {
    def apply(name: String, invokedCount: Int = 0) = {
      println("Configured Employee Settings")
      invokedCount
    }
  }

  implicit class IntTimes(x: Int) {
    def times(f: => Unit): Unit = {
      for (_ <- 0 until x) f
    }
  }

  implicit class EmployeeTimes(x: Employee) {
    def times(f: => Unit): Unit = {
      for (_ <- 0 until 5) {
        f
        x.invokedCount = x.invokedCount + 1
      }
    }
  }

  object Singleton {
    def printValue() {
      println("printValue invoked")
    }

    def apply(x: Employee) = {
      println("Configured Settings")
      x.invokedCount
    }
    def unapply(x: Int): Option[String] = Some(x.toString())
  }

  def time(): Long = {
    println("Getting time in nano seconds")
    return System.nanoTime
  }
  def delayed(t: => Long = 1) = {
    println("In delayed method")
    println("Param: " + t)
  }

  // write main method
  def main(args: Array[String]): Unit = {
    val pattern = "Scala".r
    val str = "Scala is Scalable and cool"

    println(pattern.findFirstIn(str))

    delayed(time());
    // println("Hello, world!")

    //  var myStr: String = "Hello, world!"
    //  val myInt: Int = 10

    //  myStr = "Modified World"
    //  10 times ({
    //    println(myStr)
    //  })

    4 times println("Hello, world!")
    var emp = Employee("kishore", 0)
    emp times printf("Times invoked %d\n", emp.invokedCount)

    // Singleton
    var singletonObject = Singleton(emp)
    singletonObject match {
      case Singleton(value) => {
        println("Value saved in Singleton " + value)
      }
      case _ => println("Could not resolve")
    }

    emp = Employee("kishore", 1)
    singletonObject = Singleton(emp)
    singletonObject match {
      case Singleton(value) => {
        println("Value saved in Singleton " + value)
      }
      case _ => println("Could not resolve")
    }

    // val numList = List(1,2,3,4,5,6);
    // var valueReturned = for {num <- numList; if num != 3 && num < 6} yield num

    // println(valueReturned)

  }

  class Point(xc: Int, yc: Int) {
    var x: Int = xc
    var y: Int = yc

    def move(dx: Int, dy: Int) {
      x = x + dx
      y = y + dy
      println("Point x location : " + x);
      println("Point y location : " + y);
    }
  }

  class Location(val xc: Int, val yc: Int, val zc: Int) extends Point(xc, yc) {
    var z: Int = zc

    def move(dx: Int, dy: Int, dz: Int) {
      x = x + dx
      y = y + dy
      z = z + dz
      println("Point x location : " + x);
      println("Point y location : " + y);
      println("Point z location : " + z);
    }
  }
}
