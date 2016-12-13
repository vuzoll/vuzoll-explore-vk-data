package com.github.vuzoll.explorevk.service

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

@Service
@Slf4j
class ReportFilesService {

    File createEmptyFile(String path) {
        File reportFile = new File(path)
        if (reportFile.exists()) {
            log.warn "Removing existent $reportFile.path..."
            reportFile.delete()
        }
        reportFile.createNewFile()

        return reportFile
    }
}
