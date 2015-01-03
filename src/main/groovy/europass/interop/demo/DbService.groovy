package europass.interop.demo

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import javax.inject.Inject

@javax.inject.Singleton
class DbService {

    private final Sql sql

    @Inject
    DbService(Sql sql) {
        this.sql = sql
    }

    def createTable() {
        try {
            sql.executeInsert("drop table if exists profiles")
            sql.executeInsert("""
                create table if not exists profiles (
                    id int primary key auto_increment,
                    file_name varchar(255),
                    first_name varchar(255),
                    surname varchar(255),
                    email varchar(255),
                    telephone varchar(255),
                    headline varchar(255),
                    computer_skills varchar(255)
                )
            """)
        } catch (e) {
            println e
        }
    }

    long save(Map<String, String> resume) {
        sql.executeInsert("""
            insert into profiles (
                file_name,
                first_name,
                surname,
                email,
                telephone,
                headline,
                computer_skills
            ) values (
                $resume.fileName,
                $resume.firstName,
                $resume.surname,
                $resume.email,
                $resume.telephone,
                $resume.headline,
                $resume.computerSkills
            )
        """)[0][0] as long
    }

    def update(Long id, Map<String, String> profile) {
        sql.executeUpdate("""
            update profiles set
                first_name=$profile.firstName,
                surname=$profile.surname,
                email=$profile.email,
                telephone=$profile.telephone,
                headline=$profile.headline,
                computer_skills=$profile.computerSkills
            where id = $id
        """) as long
    }

    def findById(Long id) {
        if(!id) {
            return null
        }
        def row = sql.firstRow("select * from profiles where id = $id")
        if(!row) {
            return null
        }
        return [
            id: row?.id,
            fileName: row?.file_name,
            firstName: row?.first_name,
            surname: row?.surname,
            email: row?.email,
            telephone: row?.telephone,
            headline: row?.headline,
            computerSkills: row?.computer_skills,
        ]
    }

    List<Map<String, String>> list() {
        List<Map<String, String>> resumes = sql.rows("select * from profiles").collect { GroovyRowResult row ->
            try {
                return [
                    id: row?.id,
                    fileName: row?.file_name,
                    firstName: row?.first_name,
                    surname: row?.surname,
                    email: row?.email,
                    telephone: row?.telephone,
                    headline: row?.headline,
                    computerSkills: row?.computer_skills,
                ]
            } catch (e) {
                println e
            }
        }
        return resumes
    }

}
