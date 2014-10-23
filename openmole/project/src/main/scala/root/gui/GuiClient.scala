package root.gui

import sbt._
import sbt.Keys._
import root.{ GuiDefaults, base }
import root.Libraries._
import root.ThirdParties._
import scala.scalajs.sbtplugin.ScalaJSPlugin._

object Client extends GuiDefaults {
  override val dir = super.dir / "client"

  lazy val dataui = OsgiProject("org.openmole.gui.client.dataui") settings (scalaJSSettings: _*) dependsOn
    (Ext.data) settings (
      libraryDependencies ++= Seq(scalaRxJS)
    )

  lazy val factoryui = OsgiProject("org.openmole.gui.client.factoryui") settings (scalaJSSettings: _*) dependsOn
    (dataui, Ext.data, base.Core.model, base.Core.model)

  lazy val workflow = OsgiProject("org.openmole.gui.client.workflow") settings (
    libraryDependencies ++= Seq(autowireJS, scalaTagsJS, scalaRxJS, scalajsDom, scaladget)) settings (scalaJSSettings: _*)

  lazy val core = OsgiProject("org.openmole.gui.client.core") dependsOn
    (factoryui, workflow, Shared.shared, Tools.tools) settings (
      libraryDependencies ++= Seq(autowireJS, scalaTagsJS, scalaRxJS, scalajsDom, upickleJS)) settings (scalaJSSettings: _*)
}