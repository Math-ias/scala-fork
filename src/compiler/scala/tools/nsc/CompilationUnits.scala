/* NSC -- new Scala compiler
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc

import util.{ FreshNameCreator,Position,NoPosition,SourceFile }
import scala.collection.mutable.{ LinkedHashSet, HashSet, HashMap, ListBuffer }

trait CompilationUnits { self: Global =>

  /** One unit of compilation that has been submitted to the compiler.
    * It typically corresponds to a single file of source code.  It includes
    * error-reporting hooks.  */
  class CompilationUnit(val source: SourceFile) extends CompilationUnitTrait {

    /** the fresh name creator */
    var fresh: FreshNameCreator = new FreshNameCreator.Default

    /** the content of the compilation unit in tree form */
    var body: Tree = EmptyTree

    /** representation for a source code comment, includes
     * '//' or '/*' '*/' in the value and the position
     */
    case class Comment(text: String, pos: Position)

    /** all comments found in this compilation unit */
    val comments = new ListBuffer[Comment]

//    def parseSettings() = {
//      val argsmarker = "SCALAC_ARGS"
//      if(comments nonEmpty) {
//        val pragmas = comments find (_.text.startsWith("//#")) // only parse first one
//        pragmas foreach { p =>
//          val i = p.text.indexOf(argsmarker)
//          if(i > 0)
//        }
//      }
//    }
    /** Note: depends now contains toplevel classes.
     *  To get their sourcefiles, you need to dereference with .sourcefile
     */
    val depends = new HashSet[Symbol]

    /** so we can relink
     */
    val defined = new HashSet[Symbol]

    /** Synthetic definitions generated by namer, eliminated by typer.
     */
    val synthetics = new HashMap[Symbol, Tree]

    /** things to check at end of compilation unit */
    val toCheck = new ListBuffer[() => Unit]

    def position(pos: Int) = source.position(pos)

    /** The position of a targeted type check
     *  If this is different from NoPosition, the type checking
     *  will stop once a tree that contains this position range
     *  is fully attributed.
     */
    def targetPos: Position = NoPosition

    /** The icode representation of classes in this compilation unit.
     *  It is empty up to phase 'icode'.
     */
    val icode: LinkedHashSet[icodes.IClass] = new LinkedHashSet

    def error(pos: Position, msg: String) =
      reporter.error(pos, msg)

    def warning(pos: Position, msg: String) =
      reporter.warning(pos, msg)

    def deprecationWarning(pos: Position, msg: String) =
      if (opt.deprecation) warning(pos, msg)
      else currentRun.deprecationWarnings += 1

    def uncheckedWarning(pos: Position, msg: String) =
      if (opt.unchecked) warning(pos, msg)
      else currentRun.uncheckedWarnings += 1

    def incompleteInputError(pos: Position, msg:String) =
      reporter.incompleteInputError(pos, msg)

    def comment(pos: Position, msg: String) =
      reporter.comment(pos, msg)

    /** Is this about a .java source file? */
    lazy val isJava = source.file.name.endsWith(".java")

    override def toString() = source.toString()

    def clear() {
      fresh = null
      body = null
      depends.clear
      defined.clear
    }
  }
}


