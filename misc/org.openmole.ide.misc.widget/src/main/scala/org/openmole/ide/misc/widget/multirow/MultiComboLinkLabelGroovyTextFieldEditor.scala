/*
 * Copyright (C) 2012 mathieu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) _ later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT _ WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.ide.misc.widget.multirow

import javax.swing.Icon
import org.openmole.core.model.data.IPrototype
import org.openmole.ide.misc.widget.ContentAction
import org.openmole.ide.misc.widget._
import org.openmole.ide.misc.widget.PrototypeGroovyTextFieldEditor
import org.openmole.ide.misc.widget.LinkLabel
import org.openmole.ide.misc.widget.multirow.MultiWidget._
import org.openmole.ide.misc.widget.multirow.RowWidget.Plus
import org.openmole.ide.misc.widget.multirow.RowWidget._
import scala.swing.MyComboBox
import scala.swing.event.SelectionChanged

object MultiComboLinkLabelGroovyTextFieldEditor {

  class ComboLinkLabelGroovyTextFieldEditorPanel[A](val comboContent: List[(A, IPrototype[_], ContentAction[A])],
                                                    val image: Icon,
                                                    val data: ComboLinkLabelGroovyTextFieldEditorData[A]) extends PluginPanel("wrap 3") with IPanel[ComboLinkLabelGroovyTextFieldEditorData[A]] {
    val comboBox = new MyComboBox(comboContent.sortBy { _._1.toString }.map(c ⇒ c._1)) {
      data.content match {
        case Some(x: A) ⇒
          selection.item = x
        case _ ⇒
      }
    }

    var textField = new PrototypeGroovyTextFieldEditor("Default value", data.prototype, data.editorValue)
    val linkLabel = new LinkLabel("", comboTuple._3) { icon = image }

    contents += comboBox
    contents += linkLabel
    contents += textField

    listenTo(`comboBox`)
    comboBox.selection.reactions += {
      case SelectionChanged(`comboBox`) ⇒
        val tuple = comboTuple
        linkLabel.action = tuple._3
        if (contents.size > 0) {
          contents(0) match {
            case x: PrototypeGroovyTextFieldEditor ⇒
              contents.remove(2)
              textField = new PrototypeGroovyTextFieldEditor("Default value", tuple._2, "")
              contents.insert(2, textField)
            case _ ⇒
          }
        }
    }

    def comboTuple = comboContent.filter { cc ⇒ cc._1 == comboBox.selection.item }.head

    def content = {
      val tuple = comboTuple
      new ComboLinkLabelGroovyTextFieldEditorData(tuple._2, Some(tuple._1), textField.editorText)
    }
  }

  class ComboLinkLabelGroovyTextFieldEditorData[A](val prototype: IPrototype[_],
                                                   val content: Option[A] = None,
                                                   val editorValue: String = "") extends IData

  class ComboLinkLabelGroovyTextFieldEditorFactory[A](comboContent: List[(A, IPrototype[_], ContentAction[A])],
                                                      image: Icon) extends IFactory[ComboLinkLabelGroovyTextFieldEditorData[A]] {
    def apply =
      new ComboLinkLabelGroovyTextFieldEditorPanel(comboContent,
        image,
        new ComboLinkLabelGroovyTextFieldEditorData(comboContent(0)._2,
          Some(comboContent(0)._1),
          ""))
  }
}

import MultiComboLinkLabelGroovyTextFieldEditor._
class MultiComboLinkLabelGroovyTextFieldEditor[A](title: String,
                                                  comboContent: List[(A, IPrototype[_], ContentAction[A])],
                                                  initPanels: List[ComboLinkLabelGroovyTextFieldEditorPanel[A]],
                                                  image: Icon,
                                                  minus: Minus = NO_EMPTY,
                                                  plus: Plus = ADD) extends MultiPanel(title,
  new ComboLinkLabelGroovyTextFieldEditorFactory(comboContent, image),
  initPanels,
  minus,
  plus)

//
//object MultiComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditor {
//  class Factory[A] extends IRowWidgetFactory[ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget[A]] {
//    def apply(row: ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget[A], panel: MyPanel) = {
//      import row._
//      new ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget(comboContent, initValues, image, plus)
//    }
//  }
//
//  class ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget[A](val comboContent: List[(A, IPrototype[_], ContentAction[A])],
//                                                        val initValues: (A, IPrototype[_], ContentAction[A], String),
//                                                        val image: Icon,
//                                                        val plus: Plus) extends IRowWidget2[A, String] {
//    val linkLabel = new LinkLabel("", initValues._3) { icon = image }
//    var textField = new PrototypeGroovyTextFieldEditor("Default value", initValues._2, initValues._4)
//
//    val comboBox = new MyComboBox(comboContent.sortBy { _._2.name }.map(c ⇒ c._1))
//    comboBox.selection.item = initValues._1
//
//    override val panel =
//      new RowPanel(List(comboBox, linkLabel, textField), plus) {
//        listenTo(`comboBox`)
//        comboBox.selection.reactions += {
//          case SelectionChanged(`comboBox`) ⇒
//            val it = comboContent.filter { cc ⇒ cc._1 == comboBox.selection.item }.head
//            linkLabel.action = it._3
//            contents(0) match {
//              case x: MigPanel ⇒
//                x.contents.remove(2)
//                textField = new PrototypeGroovyTextFieldEditor("Default value", it._2, "")
//                x.contents.insert(2, textField)
//            }
//        }
//      }
//
//    override def content: (A, String) = (comboBox.selection.item, textField.editorText)
//  }
//}
//
//import MultiComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditor._
//
//class MultiComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditor[A](title: String,
//                                                  rWidgets: List[ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget[A]],
//                                                  factory: IRowWidgetFactory[ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget[A]],
//                                                  minus: Minus = CLOSE_IF_EMPTY,
//                                                  plus: Plus = ADD) extends MultiWidget(title, rWidgets, factory, minus) {
//
//  def this(title: String,
//           comboContent: List[(A, IPrototype[_], ContentAction[A])],
//           initValues: List[(A, IPrototype[_], ContentAction[A], String)],
//           image: Icon,
//           factory: IRowWidgetFactory[ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget[A]],
//           minus: Minus,
//           plus: Plus) = this(title,
//    if (initValues.isEmpty) {
//      List(new ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget(comboContent,
//        (comboContent(0)._1, comboContent(0)._2, comboContent(0)._3, ""),
//        image,
//        plus))
//    } else initValues.map {
//      case (a, m, action, s) ⇒ new ComboLinkLabelGroovyTextFieldEditorGroovyTextFieldEditorRowWidget(comboContent, (a, m, action, s), image, plus)
//    }, factory, minus, plus)
//  def this(title: String,
//           initValues: List[(A, IPrototype[_], ContentAction[A], String)],
//           comboContent: List[(A, IPrototype[_], ContentAction[A])],
//           image: Icon) = this(title,
//    comboContent,
//    initValues,
//    image,
//    new Factory[A], CLOSE_IF_EMPTY, ADD)
//
//  def content = rowWidgets.map(_.content).toList
//}