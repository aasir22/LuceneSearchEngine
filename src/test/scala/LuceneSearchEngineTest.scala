import com.lucenesearch.LuceneSearchEngine
import org.scalatest.funsuite.AnyFunSuite

class LuceneSearchEngineTest extends AnyFunSuite{
  val fuzzyQueryOutput = "1.heart.txt Score :0.42175457 Array(death , health , healthy )\n2.diabetes.txt Score :0.39318877 Array(health , healthy )\n3.MusclePain.txt Score :0.31447878 Array(health , healthy )\n4.MentalHealth.txt Score :0.27454004 Array(health )\n5.tumor.txt Score :0.16454676 Array(death , healthy )\n"
  val lucene = new LuceneSearchEngine
  lucene.createIndexFiles ("dataFiles")

  test("Term query test case"){
    assert(lucene.searchIndex("tumor","termquery") == "1.tumor.txt Score :1.2193596 Array(tumor )\n")
  }
  test("Term query negative test case 1"){
    assert(lucene.searchIndex(" ","termquery") == "No files founded")
  }
  test("Term query negative test case 2"){
    assert(lucene.searchIndex("","termquery") == "No files founded")
  }


  test("And query test case"){
    assert(lucene.searchIndex("blood@@tumor","and query") == "1.tumor.txt Score :1.479102 Array(blood , tumor )\n")
  }
  test("And query negative test case 1"){
    assert(lucene.searchIndex(" ","and query") == "No files founded")
  }
  test("And query negative test case 2"){
    assert(lucene.searchIndex("","and query") == "No files founded")
  }

  test("Or query test case"){
    assert(lucene.searchIndex("leg##stomach","Or query") == "1.heart.txt Score :0.58411413 Array(leg )\n2.tumor.txt Score :0.55320334 Array(stomach )\n")
  }
  test("Or query negative test case 1"){
    assert(lucene.searchIndex(" ","Or query") == "No files founded")
  }
  test("Or query negative test case 2"){
    assert(lucene.searchIndex("","Or query") == "No files founded")
  }

  test("Fuzzy query test case"){
    assert(
      lucene.searchIndex("health","fuzzy Query") == fuzzyQueryOutput
    )
  }
  test("Fuzzy query negative test case"){
    assert(lucene.searchIndex("health"," ") == "Please give the correct search type")
  }

  test("phrase query test case"){
    assert(lucene.searchIndex("heart~attack","phrase query") == "1.heart.txt Score :1.8908063 Array(heart attack)\n")
  }

  test("prefix query test case"){
    assert(lucene.searchIndex("hear","prefix query") == "1.heart.txt Score :1.0 Array(Heart)\n2.diabetes.txt Score :1.0 Array(heart)\n")
  }
  test("prefix query negative test case 1"){
    assert(lucene.searchIndex(" ","prefix query") == "No files founded")
  }

  test("wildcard query test case"){
    assert(lucene.searchIndex("he?rt","wildcard query") == "1.heart.txt Score :1.0 Array(Heart)\n2.diabetes.txt Score :1.0 Array(heart)\n")
  }
  test("wildcard query negative test case 1"){
    assert(lucene.searchIndex(" ","wildcard query") == "No files founded")
  }

}