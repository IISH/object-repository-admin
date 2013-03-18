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

    final static float rotationToLandscape = Math.PI * 2 * 0.75
    private static float documentRatio = 210f / 297f  // A4

    /**
     * writePdfFile
     *
     * Collect all pid values in the required order from the specified bucket
     * Offer a standard A4 document
     * If the width of the image is smaller than the height, we scale to fit the width
     * If the width of the image is larger than the height, we use a landscape view; rotate 270 degrees and scale to fit the height
     *
     * @param na
     * @param pid
     * @param use
     */
    def list(def writer, String na, String objid, String bucket) {

        final Document document = new Document(PageSize.A4, 0, 0, 0, 0)
        final float documentWidth = document.getPageSize().getWidth() + document.getPageSize().getBorderWidthLeft() + document.getPageSize().getBorderWidthRight()
        final float documentHeight = document.getPageSize().getHeight() + document.getPageSize().getBorderWidthTop() + document.getPageSize().getBorderWidthBottom()
        final PdfWriter pdfWriter = PdfWriter.getInstance(document, writer)
        document.open()
        gridFSService.listPdf(na, objid, bucket).each {
            final String access = policyService.getPolicy(it).getAccessForBucket(bucket)
            final Boolean denied = policyService.denied(access, na)
            if (denied) {
                document.add(new Paragraph("Not allowed to render page. Access " + access))
            }
            else if (it.contentType.startsWith('image')) {
                def image = null
                try {
                    image = Image.getInstance(IOUtils.toByteArray(it.inputStream))
                } catch (Exception e) {
                    document.add(new Paragraph(e.message))
                }
                if (image) {
                    float imageWidth = image.getWidth()
                    float imageHeight = image.getHeight()
                    if (imageWidth > imageHeight) { // A4 to square
                        imageHeight = imageWidth;
                        imageWidth = image.getHeight()
                        image.setRotation(rotationToLandscape) // landscape... Assume clockwise rotation
                    }
                    // Fit the image onto the A4 page
                    float imageRatio = imageWidth / imageHeight
                    float scale = (imageRatio > documentRatio) ? documentWidth * 100 / imageWidth : documentHeight * 100 / imageHeight
                    image.scalePercent(scale)
                    document.add(image)
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
