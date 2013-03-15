package org.objectrepository.pdf

import com.lowagie.text.Document
import com.lowagie.text.PageSize
import com.lowagie.text.pdf.PdfWriter
import com.mongodb.DBCursor
import com.lowagie.text.Image
import org.apache.commons.io.IOUtils
import com.lowagie.text.Paragraph

class PdfService {

    def gridFSService
    def mongo
    static String OR = "or_"

    /**
     * writePdfFile
     *
     * Collect all pid values in the required order from the specified bucket
     *
     * @param na
     * @param pid
     * @param use
     */
    def list(def writer, String na, String objid, String bucket) {

        final DBCursor cursor = mongo.getDB(OR + na).getCollection(bucket + '.files').find(['metadata.objid': objid], ['metadata.pid': 1]).sort(['metadata.seq': 1])
        final Document document = new Document(PageSize.A4, 0, 0, 0, 0)
        final float width = document.getPageSize().getWidth() + document.getPageSize().getBorderWidthLeft() + document.getPageSize().getBorderWidthRight()
        final PdfWriter pdfWriter = PdfWriter.getInstance(document, writer)
        document.open();
        while (cursor.hasNext()) {
            final pid = cursor.next().metadata.pid
            final file = gridFSService.findByPid(pid, bucket)
            if (file.contentType.startsWith('image')) {
                final image2 = Image.getInstance(IOUtils.toByteArray(file.inputStream))
                image2.scalePercent(width * 100 / image2.getWidth())
                document.add(image2)
            } else {
                document.add(new Paragraph("Cannot render page."))
            }
            document.newPage()
        }
        document.close()
        pdfWriter.close()
    }
}
