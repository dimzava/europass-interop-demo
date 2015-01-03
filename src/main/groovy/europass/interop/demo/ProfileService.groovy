package europass.interop.demo

import com.itextpdf.text.pdf.*
import groovy.util.slurpersupport.GPathResult
import ratpack.form.UploadedFile

import javax.inject.Inject

class ProfileService {

    static final String EUROPASS_XML_ATTACHMENT_NAME = 'Europass-XML-Attachment.xml'
    private final DbService dbService
    private final ConversionService conversionService

    @Inject
    ProfileService(DbService dbService, ConversionService conversionService) {
        this.dbService = dbService
        this.conversionService = conversionService
    }

    long upload(UploadedFile file) {
        if(isXml(file)) {
            return processXml(file.fileName, file?.inputStream)
        } else if(isPdf(file)) {
            return processPdf(file.fileName, file?.inputStream)
        } else {
            println 'not xml or pdf!'
            // ignore
        }
    }

    byte[] exportToPdf(Long id) {
        Map<String, String> profile = dbService.findById(id)
        return conversionService.toPdf(profile)
    }

    static def isXml(UploadedFile file) {
        return file?.contentType?.type?.contains('xml')
    }

    static def isPdf(UploadedFile file) {
        return file?.contentType?.type?.contains('pdf')
    }

    long processXml(String fileName, InputStream xmlStream) {
        Map<String, String> profile = [:]
        GPathResult skillsPassport = new XmlSlurper().parse(xmlStream)
        profile['fileName'] = fileName
        profile['firstName'] = skillsPassport.LearnerInfo.Identification.PersonName.FirstName.text()
        profile['surname'] = skillsPassport.LearnerInfo.Identification.PersonName.Surname.text()
        profile['email'] = skillsPassport.LearnerInfo.Identification.ContactInfo.Email.Contact.text()
        profile['telephone'] = skillsPassport.LearnerInfo.Identification.ContactInfo.TelephoneList[0].Telephone.Contact.text()
        profile['headline'] = skillsPassport.LearnerInfo.Headline.Description.Label.text()
        profile['computerSkills'] = skillsPassport.LearnerInfo.Skills.Computer.Description.text()
        return dbService.save(profile)
    }

    long processPdf(String fileName, InputStream pdfStream) {
        InputStream xmlStream = extractXmlFromPdf(pdfStream)
        return processXml(fileName, xmlStream)
    }

    static InputStream extractXmlFromPdf(InputStream pdfStream) {
        PdfArray fileSpecs = getFileSpecs(pdfStream)
        PRStream stream
        for (int i = 0; i < fileSpecs.size(); ) {
            PdfDictionary fileSpec = fileSpecs?.getAsDict(i++)
            PdfDictionary refs = fileSpec?.getAsDict(PdfName.EF)
            PdfName key = refs?.keys?.first()
            String filename = fileSpec?.getAsString(key)?.toString()
            if(filename != EUROPASS_XML_ATTACHMENT_NAME) {
                continue
            }
            stream = (PRStream)PdfReader.getPdfObject(refs.getAsIndirectObject(key))
        }
        return new ByteArrayInputStream(PdfReader.getStreamBytes(stream))
    }

    static PdfArray getFileSpecs(InputStream pdfStream) {
        PdfReader reader = new PdfReader(pdfStream)
        PdfDictionary root = reader.getCatalog()
        PdfDictionary names = root.getAsDict(PdfName.NAMES)
        PdfDictionary embedded = names.getAsDict(PdfName.EMBEDDEDFILES)
        return embedded.getAsArray(PdfName.NAMES)
    }

}
