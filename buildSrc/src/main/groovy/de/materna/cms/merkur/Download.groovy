package de.materna.cms.merkur

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task-Klasse zum Download von Dateien
 */
class Download extends DefaultTask {
  @Input
  String sourceUrl

  @OutputFile
  File target

  @TaskAction
  void download() {
	ant.get(src: sourceUrl, dest: target)
  }
}
