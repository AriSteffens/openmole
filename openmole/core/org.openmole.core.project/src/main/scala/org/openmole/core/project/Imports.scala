/**
 * Created by Romain Reuillon on 27/01/16.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmole.core.project

import java.io.File
import org.openmole.tool.file._

object Imports {

  lazy val parent = "_parent_"
  def separators = Array('\n', ';')
  def space = Set('\t', ' ')
  def removeHeadingSpaces(s: String) = s.dropWhile(space.contains)
  def trimSpaces(s: String) = removeHeadingSpaces(removeHeadingSpaces(s).reverse).reverse
  def arrow = Set("=>", "⇒")

  def parseImport(i: String): Seq[Import] = {
    val (namespace, entity) =
      if (i.contains('.')) {
        val parts = i.split('.')
        (parts.dropRight(1).toSeq, parts.last)
      }
      else (Seq.empty, i)

    val multiple = """\{(.*)\}""".r
    val alias = """(.*)(=>|⇒)(.*)""".r

    def nameOrWideCard(e: String) =
      trimSpaces(e) match {
        case "_" ⇒ WildCard
        case n   ⇒ Alias(trimSpaces(n), trimSpaces(n))
      }

    val entities: Seq[ImportSelector] = trimSpaces(entity) match {
      case multiple(e) ⇒
        val parsedMultiple =
          e.split(',') map {
            case alias(from, _, to) ⇒
              trimSpaces(to) match {
                case "_" ⇒ WildCardBut(trimSpaces(from))
                case _   ⇒ Alias(trimSpaces(from), trimSpaces(to))
              }
            case s ⇒ nameOrWideCard(s)
          }

        parsedMultiple.toList match {
          case Nil ⇒ Seq.empty
          case multipleImports ⇒
            val aliases =
              multipleImports.filter {
                case _: WildCardBut ⇒ false
                case _              ⇒ true
              }

            multipleImports.last match {
              case WildCard ⇒
                val wildCardBut =
                  multipleImports.collect {
                    case x: WildCardBut ⇒ x
                    case x: Alias       ⇒ WildCardBut(x.from)
                  }.reduceLeft { (w1, w2) ⇒ WildCardBut(w1.names ++ w2.names: _*) }
                aliases.dropRight(1) ++ Seq(wildCardBut)
              case _ ⇒ aliases
            }
        }
      case e ⇒
        def parsed = nameOrWideCard(e)
        Seq(parsed)
    }

    entities.map(Import(namespace, _))
  }

  def parseImports(script: String) = {
    def lines = script.split(separators)
    def imports = lines.map(removeHeadingSpaces).filter(_.startsWith("import")).map(i ⇒ removeHeadingSpaces(i.drop("import".size)))
    imports.flatMap(parseImport)
  }

  def level1ImportedFiles(imports: Seq[Import], directory: File): Seq[ImportedFile] = {
    def matchingFileInStableIdentifier(imp: Import): Option[ImportedFile] =
      (1 to imp.stableIdentifier.size).map { i ⇒
        val part = imp.stableIdentifier.take(i)
        val stableIdentifier = part.dropRight(1)
        val path = toFile(directory, stableIdentifier ++ part.lastOption.map(_ + Project.scriptExtension))
        ImportedFile(stableIdentifier, path)
      }.find { importedFile ⇒ Project.isScript(importedFile.file) }

    def matchingFileInSelector(imp: Import): Seq[ImportedFile] =
      imp match {
        case Import(stableIdentifier, Alias(from, _)) ⇒
          val file = toFile(directory, stableIdentifier) / (from + Project.scriptExtension)
          if (file.exists) Seq(ImportedFile(stableIdentifier, file)) else Seq.empty
        case Import(stableIdentifier, WildCard) ⇒
          listScripts(toFile(directory, stableIdentifier)) map {
            script ⇒ ImportedFile(stableIdentifier, script)
          }
        case _ ⇒ Seq.empty
      }

    imports flatMap { imp ⇒
      val inSelector = matchingFileInSelector(imp)
      if (!inSelector.isEmpty) inSelector else matchingFileInStableIdentifier(imp).toSeq
    }
  }

  def importedFiles(script: File): Seq[ImportedFile] = {
    var alreadyImported = List.empty[File]

    def importedFiles0(script: File): Seq[ImportedFile] = {
      val imported = level1ImportedFiles(parseImports(script.content), script.getParentFileSafe)
      val newlyImported = imported.filter(i ⇒ !alreadyImported.exists(_.getCanonicalPath == i.file.getCanonicalPath))
      alreadyImported = newlyImported.map(_.file).toList ::: alreadyImported
      newlyImported ++ newlyImported.flatMap { i ⇒ importedFiles0(i.file) }
    }

    importedFiles0(script)
  }

  def importTree(script: File) = Tree.insertAll(importedFiles(script))

  case class Import(stableIdentifier: Seq[String], importSelector: ImportSelector)

  sealed trait ImportSelector
  case class Alias(from: String, to: String) extends ImportSelector
  case class WildCardBut(names: String*) extends ImportSelector
  case object WildCard extends ImportSelector

  case class ImportedFile(stableIdentifier: Seq[String], file: File)

  def toFile(dir: File, path: Seq[String]) =
    path.foldLeft(dir) {
      (d, n) ⇒
        n match {
          case `parent` ⇒ d.getParentFile
          case _        ⇒ d / n
        }
    }

  def listScripts(dir: File) = dir.listFilesSafe(Project.isScript _)

  object Tree {
    def empty = Tree(Seq.empty, Seq.empty)
    def insertAll(importedFiles: Seq[ImportedFile]) = {
      def insertAll0(importedFiles: List[ImportedFile], tree: Tree): Tree =
        importedFiles match {
          case Nil    ⇒ tree
          case h :: t ⇒ insertAll0(t, insert(h, tree))
        }
      insertAll0(importedFiles.toList, Tree.empty)
    }

    def insert(importedFile: ImportedFile, tree: Tree): Tree = {
      def emptyTree(names: Seq[String]): Tree =
        names match {
          case Nil    ⇒ Tree(Seq(importedFile.file), Seq.empty)
          case h :: t ⇒ Tree(Seq.empty, Seq(NamedTree(h, emptyTree(t))))
        }

      importedFile.stableIdentifier.toList match {
        case Nil ⇒ tree.copy(tree.files ++ Seq(importedFile.file))
        case h :: t ⇒
          tree.children.indexWhere(_.name == h) match {
            case -1 ⇒ tree.copy(children = tree.children ++ Seq(NamedTree(h, emptyTree(t))))
            case i ⇒
              def currentChildTree: NamedTree = tree.children(i)
              def updated = currentChildTree.copy(tree = insert(importedFile.copy(stableIdentifier = t), currentChildTree.tree))
              tree.copy(children = tree.children.updated(i, updated))
          }
      }
    }
  }

  case class Tree(files: Seq[File], children: Seq[NamedTree])
  case class NamedTree(name: String, tree: Tree)

}
