/*
 * Copyright (C) 30/07/14 // mathieu.leclaire@openmole.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmole.gui.bootstrap

import java.io.File
import java.net.URL

import org.openmole.gui.client.dataui.DataUIs
import org.openmole.gui.client.factoryui.ClientFactories
import org.openmole.gui.client.workflow.GraphCreator
import org.openmole.gui.server.core.{ JSPack, GUIServer }
import org.openmole.gui.client.core.GUIClient
import org.osgi.framework.Bundle
import org.openmole.misc.pluginmanager.PluginManager
import org.openmole.misc.workspace.Workspace
import org.openmole.misc.tools.io.FileUtil
import org.openmole.misc.tools.io.FileUtil._
import org.osgi.framework.Bundle
import scala.collection.JavaConverters._
import org.openmole.misc.fileservice._

object Bootstrap {

  val webui = Workspace.file("webui")
  val jsSrc = new File(webui, "js/src")
  val jsCompiled = new File(webui, "js/compiled")
  val webapp = new File(webui, "webapp")
  jsSrc.mkdirs
  jsCompiled.mkdirs
  webapp.mkdirs

  new File(webapp, "js").mkdirs
  new File(webapp, "css").mkdirs
  new File(webapp, "fonts").mkdirs
  new File(webapp, "WEB-INF").mkdirs

  def init(pluginBundles: List[Bundle], optimized: Boolean = true) = {

    //Copy all the fixed resources in the workspace if required

    val thisBundle = PluginManager.bundleForClass(classOf[GUIServer])
    copyURL(thisBundle.findEntries("/", "*.js", true).asScala)
    copyURL(thisBundle.findEntries("/", "*.css", true).asScala)
    copyURL(thisBundle.findEntries("/", "web.xml", true).asScala)

    // Extract and copy all the .sjsir files from bundles to src
    val bundles = pluginBundles ++ Seq(
      PluginManager.bundleForClass(classOf[GUIClient]),
      PluginManager.bundleForClass(classOf[DataUIs]),
      PluginManager.bundleForClass(classOf[ClientFactories]),
      PluginManager.bundleForClass(classOf[GraphCreator])
    )

    bundles.map { b ⇒
      b.findEntries("/", "*.sjsir", true)
    }.filterNot {
      _ == null
    }.flatMap {
      _.asScala
    }.map { u ⇒
      u.openStream.copy(new java.io.File(jsSrc, u.getFile.split("/").tail.mkString("-")))
    }

    //Generates js files if
    // - the sources changed or
    // - the optimized js does not exists in optimized mode or
    // - the not optimized js does not exists in not optimized mode
    jsSrc.updateIfChanged(JSPack(_, jsCompiled, optimized))
    if (optimized && !new File(jsCompiled, JSPack.OPTIMIZED).exists ||
      !optimized && !new File(jsCompiled, JSPack.NOT_OPTIMIZED).exists)
      JSPack(jsSrc, jsCompiled, optimized)

  }

  def copyURL(url: Iterator[URL]) = {
    url.foreach { u ⇒
      u.openStream.copy(new File(webui, u.getFile))
    }
  }

}