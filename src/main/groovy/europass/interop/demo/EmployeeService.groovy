package europass.interop.demo

import com.google.inject.Inject
import wslite.rest.ContentType
import wslite.rest.RESTClient

@javax.inject.Singleton
class EmployeeService {
    private final DbService resumeService

    @Inject
    EmployeeService(DbService resumeService) {
        this.resumeService = resumeService
    }

    def list() {
        return [
            [
                firstName: 'Anna', surname: 'Evans', email: 'aeva@companyx.eu', telephone: '+30 6974123456', jobTitle: 'Art Director', department: 'UX',
                workExperience: [
                    [fromYear: '2010', fromMonth: '01'], [fromYear: '2007', fromMonth: '05', toYear: '2009', toMonth: '12', employer: 'Web Design, Ltd.', jobTitle: 'Senior Designer']
                ]
            ],
            [
                firstName: 'Andrew', surname: 'Alexander', email: 'aale@companyx.eu', telephone: '+30 6974321987', jobTitle: 'Software Architect', department: 'Engineering',
                workExperience: [
                    [fromYear: '2012', fromMonth: '09'], [fromYear: '2009', fromMonth: '01', toYear: '2012', toMonth: '08', employer: 'Software House', jobTitle: 'Senior Software Engineer']
                ]
            ],
            [
                firstName: 'Norma', surname: 'Wood', email: 'nwoo@companyx.eu', telephone: '+30 69768002347', jobTitle: 'Network Engineer', department: 'Engineering',
                workExperience: [
                    [fromYear: '2013', fromMonth: '03'], [fromYear: '2012', fromMonth: '01', toYear: '2013', toMonth: '02', employer: 'Network Ops.', jobTitle: 'Junior Network Engineer']
                ]
            ],
            [
                firstName: 'Jonathan', surname: 'Hill', email: 'jhil@companyx.eu', telephone: '+30 69768111117', jobTitle: 'Software Developer', department: 'Engineering',
                workExperience: [
                    [fromYear: '2014', fromMonth: '06']
                ]
            ],
            [
                firstName: 'Laura', surname: 'Cox', email: 'lcox@companyx.eu', telephone: '+30 6974321789', jobTitle: 'Social Media Expert', department: 'Marketing',
                workExperience: [
                    [fromYear: '2013', fromMonth: '11']
                ]
            ]
        ]
    }

    def findByIndex(int index) {
        Map<String, String> employee = list().get(index)
        employee['xml'] = getXml(employee)
        return employee
    }

    def getXml(Map<String, String> employee) {
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
                            <FirstName>${employee.firstName}</FirstName>
                            <Surname>${employee.surname}</Surname>
                        </PersonName>
                        <ContactInfo>
                            <Email>
                                <Contact>${employee.email}</Contact>
                            </Email>
                            <TelephoneList>
                                <Telephone>
                                    <Contact>${employee.telephone}</Contact>
                                    <Use>
                                        <Code>work</Code>
                                    </Use>
                                </Telephone>
                            </TelephoneList>
                        </ContactInfo>
                    </Identification>
                    <Headline>
                        <Type>
                            <Code>position</Code>
                            <Label>POSITION</Label>
                        </Type>
                        <Description>
                            <Label>${employee.jobTitle}</Label>
                        </Description>
                    </Headline>
                    <WorkExperienceList>
                        <WorkExperience>
                            <Period>
                                <From year="${employee.workExperience[0]['fromYear']}" month="--${employee.workExperience[0]['fromMonth']}" />
                                <Current>true</Current>
                            </Period>
                            <Position>
                                <Label>${employee.jobTitle}</Label>
                            </Position>
                            <Employer>
                                <Name>CompanyX.eu</Name>
                            </Employer>
                        </WorkExperience>
                        ${previousWorkExperience(employee)}
                    </WorkExperienceList>
                </LearnerInfo>
            </SkillsPassport>"""
    }

    String previousWorkExperience(Map<String, String> employee) {
        List<Map<String, String>> workExperience = employee.workExperience
        StringBuilder sb = new StringBuilder('')
        if(workExperience.size() > 1) {
            workExperience.tail().each {
               sb.append(
                """
                <WorkExperience>
                    <Period>
                        <From year="${it['fromYear']}" month="--${it['fromMonth']}" />
                        <To year="${it['toYear']}" month="--${it['toMonth']}"/>
                        <Current>false</Current>
                    </Period>
                    <Position>
                        <Label>${it.jobTitle}</Label>
                    </Position>
                    <Employer>
                        <Name>${it.employer}</Name>
                    </Employer>
                </WorkExperience>
                """
               )
            }
        } else {
            return ''
        }
        return sb.toString()
    }
}
