package europass.interop.demo

import com.google.common.html.HtmlEscapers
import wslite.rest.ContentType
import wslite.rest.RESTClient

@javax.inject.Singleton
class ConversionService {

    byte[] toPdf(Map<String, String> profile) {
        def client = new RESTClient("https://europass.cedefop.europa.eu/rest/v1")
        def response = client.post(path: '/document/to/pdf-cv') {
            type ContentType.XML
            text getXml(profile)
        }
        return response.data
    }

    def getXml(Map<String, String> profile) {
        return """<?xml version='1.0' encoding='UTF-8'?>
            <SkillsPassport xmlns="http://europass.cedefop.europa.eu/Europass" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://europass.cedefop.europa.eu/Europass http://europass.cedefop.europa.eu/xml/v3.1.2/EuropassSchema.xsd" locale="en">
                <DocumentInfo>
                    <DocumentType>ECV</DocumentType>
                    <CreationDate>2014-09-26T16:56:21.609Z</CreationDate>
                    <LastUpdateDate>2014-09-26T17:01:21.336Z</LastUpdateDate>
                    <XSDVersion>V3.1</XSDVersion>
                    <Generator>EWA</Generator>
                    <Comment>Europass CV</Comment>
                </DocumentInfo>
                <LearnerInfo>
                    <Identification>
                        <PersonName>
                            <FirstName>${profile.firstName}</FirstName>
                            <Surname>${profile.surname}</Surname>
                        </PersonName>
                        <ContactInfo>
                            <Email>
                                <Contact>${profile.email}</Contact>
                            </Email>
                            <TelephoneList>
                                <Telephone>
                                    <Contact>${profile.telephone}</Contact>
                                    <Use>
                                        <Code>home</Code>
                                    </Use>
                                </Telephone>
                            </TelephoneList>
                        </ContactInfo>
                    </Identification>
                    <Headline>
                        <Type>
                            <Code>${profile.jobTitle ? 'position' : 'job_applied_for'}</Code>
                        </Type>
                        <Description>
                            <Label>${profile.headline}</Label>
                        </Description>
                    </Headline>
                    ${getWorkExperienceList(profile.jobTitle)}
                    <Skills>
                        <Computer>
                            <Description>${HtmlEscapers.htmlEscaper().escape(profile.computerSkills)}</Description>
                        </Computer>
                    </Skills>
                </LearnerInfo>
            </SkillsPassport>"""
    }

    String getWorkExperienceList(String jobTitle) {
        if(jobTitle) {
            return """
                <WorkExperienceList>
                    <WorkExperience>
                        <Period>
                            <From year="2013" month="--03" day="-----" />
                            <Current>true</Current>
                        </Period>
                        <Position>
                            <Label>${jobTitle}</Label>
                        </Position>
                        <Employer>
                            <Name>MyCompany.eu</Name>
                        </Employer>
                    </WorkExperience>
                </WorkExperienceList>
            """
        } else {
            ''
        }
    }

}
