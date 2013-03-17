package org.objectrepository.pdf

import com.lowagie.text.Document
import com.lowagie.text.PageSize
import com.lowagie.text.pdf.PdfWriter
import com.mongodb.DBCursor
import com.lowagie.text.Image
import org.apache.commons.io.IOUtils
import com.lowagie.text.Paragraph
import com.mongodb.BasicDBObject
import org.objectrepository.util.Normalizers
import com.mongodb.gridfs.GridFSFile
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.gridfs.GridFS

class PdfService {

    def gridFSService
    def policyService

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

        def list = gridFSService.listPdf(na, objid, bucket)
        final Document document = new Document(PageSize.A4, 0, 0, 0, 0)
        final float width = document.getPageSize().getWidth() + document.getPageSize().getBorderWidthLeft() + document.getPageSize().getBorderWidthRight()
        final PdfWriter pdfWriter = PdfWriter.getInstance(document, writer)
        document.open();
        list.each {
            final String access = policyService.getPolicy(it).getAccessForBucket(bucket)
            final Boolean denied = policyService.denied(access, na)
            if (denied) {
                document.add(new Paragraph("Not allowed to render page. Access " + access))
            }
            else if (it.contentType.startsWith('image')) {
                final image2
                try {
                    image2 = Image.getInstance(IOUtils.toByteArray(it.inputStream))
                    float r = width * 100 / image2.getWidth()
                    image2.scalePercent(r)
                    document.add(image2)
                } catch (Exception e) {
                    document.add(new Paragraph(e.message))
                }
            } else {
                document.add(new Paragraph("Cannot render page."))
            }
            document.newPage()
        }
        document.close()
        pdfWriter.close()
    }
}
