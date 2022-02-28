import com.lucenesearch.LuceneSearchEngine
import org.scalatest.funsuite.AnyFunSuite

class LuceneSearchEngineTest extends AnyFunSuite{
  val fuzzyQueryOutput = "1.heart.txt Score :59.242672 Array(content:death in 1, content:health in 1, content:healthy in 1, content:heart in 1)\n2.diabetes.txt Score :50.259747 Array(content:health in 0, content:healthy in 0, content:heart in 0)\n3.MusclePain.txt Score :48.601265 Array(content:heal in 3, content:health in 3, content:healthy in 3, content:heat in 3)\n4.MentalHealth.txt Score :27.454004 Array(content:health in 2)\n5.tumor.txt Score :16.454676 Array(content:death in 4, content:healthy in 4)\n"
  val lucene = new LuceneSearchEngine
  lucene.createIndexFiles ("dataFiles",canRemoveOldIndex = true)

  test("Term query test case"){
    assert(lucene.searchIndex("tumor","termquery") == "1.tumor.txt Score :121.93597 Array(content:tumor in 4)\n")
  }
  test("Term query negative test case 1"){
    assert(lucene.searchIndex(" ","termquery") == "No files founded")
  }
  test("Term query negative test case 2"){
    assert(lucene.searchIndex("","termquery") == "No files founded")
  }

  test("And query test case"){
    assert(lucene.searchIndex("blood@@tumor","and query") == "1.tumor.txt Score :147.9102 Array(content:blood in 4, content:tumor in 4)\n")
  }
  test("And query negative test case 1"){
    assert(lucene.searchIndex(" ","and query") == "No files founded")
  }
  test("And query negative test case 2"){
    assert(lucene.searchIndex("","and query") == "No files founded")
  }

  test("Or query test case"){
    assert(lucene.searchIndex("leg##stomach","Or query") == "1.heart.txt Score :58.411415 Array(content:leg in 1)\n2.tumor.txt Score :55.320335 Array(content:stomach in 4)\n")
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

}