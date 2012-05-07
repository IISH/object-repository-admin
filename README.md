#The object repository faq

##What is the object repository ?
The object repository is a collection of services:

 - ftp upload of files to a staging area (temporary storage)
 - ingesting those files into persistant storage
 - derivative generation
 - persistant identifier creation.
 - an administration interface plus account system to manage the above services

##How to get a CP administrator account for the administration interface?
The person responsible for their organization's digital workflow should have access to the
 administration interface using a CP administrator account. The repository administrator
 can set such accounts.

 After that, a CP administrator can make CP user accounts. Each of those CP user acount can be given a CP administrator
 role.

##How to get a FTP account ?
After logging in with an CP Administrator account, the user should select the tab **Users and Groups** and **add** to
make a new ftp account. The CP user for which the ftp account is intended, will receive a notification e-mail. After
that the file upload can commence immediatly.

##What are the ftp connection settings ?
Before your files can be ingested by the object repository, they need to be uploaded
 onto the staging area. This can be done using a secure FTP connection with the accounts
 created by a CP administrator. Typically such accounts are intended for both
 manual and automated upload procedures.

 You can use FileZilla or any other ftp client that supports an **explicit FTP over TLS** connection
 with **passive mode** enabled. The default FTP port is 21.

##How should the files we upload be structured ?
After a CP user logs in with a ftp-account, the first directory visible is
the **home directory**.

The user should at least upload one **main folder** containing files in the
home directory. This example illustrates the upload of three **main folders**: FolderA, FolderB and FolderC.

    Your home directory /

        /folderA  => A main folder
        /folderA/instruction.xml => A processing instruction
        /folderA/Apple.tiff  => Digital file
        /folderA/Apple.tiff.md5 ==> An automatically generated file.

        /folderB => Another main folder
        /folderB/instruction.xml
        /folderB/Rear.tiff
        /folderB/Rear.tiff.md5

        /folderC -> Yet another main folder
        /folderC/SubfolderD/
        /folderC/Banana.tiff
        /folderC/Banana.tiff.md5
        /folderC/instruction.xml
        /folderC/folderD/Potato.tiff
        /folderC/folderD/Potato.md5

NOTE: any folder or file that starts with a dot, will be ignored by the ingest procedures.
Also any digital file placed right in the home directory and not in a main folder will be ignored.

As a rule (and avoid confusion) use the CP user account to upload files and XML processing instructions with. Not your
CP administrator account.

##Why do all my files have an identically named file with the extension ".md5" ?
For each file you upload to the staging area, the FTP server produces a md5 checksum and write this to a file with
an .md5 extension. This checksum is compared the one you deliver in a processing instruction in order to
assert integrity.

NOTE: you should not delete such automatically created files. If you do; the ingest of your files can take a long time.

##What is needed to ingest digital content ?

###Digital files obviously
Each main folder holds digital files and possibly more subfolders containing files again.

###A processing instruction
The main folder ought to contain an XML document with the filename **instruction.xml** that tells the Object repository
ingest and dissemination parts how to treat the files.

## What is or makes a processing instruction?
The administration interface and an XML processing instruction together makes it all work.
An instruction has required and optional values. Those can be set at three levels:

###1. profile
Via the administration interface select **profile**. All default instruction settings can be managed there by a CP
administrator. It makes sense therefore, to place all your broad access policies and most frequently used file content
values here. Settings in the profile substitute any absent values in the XML processing instruction.

###2. instruction.xml
The instruction.xml main element named **instruction** contains attributes identical to those you set in the profile.
Any setting here will override those in the profile. Typically you would set values here that are exceptions to your
profile's general settings.

###3. stagingfile
In the instruction.xml's child elements named **stagingfile**. A stagingfile element represents the file and any
settings here will override those in the main element and profile.

Note: the stagingfile is the only element that has some unique settings not present in the profile or instruction, such
as md5, location and pid value.

In short
Profile settings provide reasonable defaults for an organizations processing instruction.
Instruction settings in an instruction.xml document override profile values and provide specific defaults for stagingfiles.
Stagingfile settings in their final turn override instruction and profile settings. The smallest always win.

##The values you should take a look at

###action (profile, instruction and stagingfile elements)
This controls how the ingest and dissemination procedures handles the file.

    action='upsert' ( default )
    Will add the file with the given pid. If a file with the pid already exists, it will be updated with the new file.
    
    action='add'
    Will add a file with the given pid. But error will occur when the pid already exists in the object repository
    
    action='update'
    Will update a file with the given pid. But error will occur if the pid does not exists in the object
    repository
    
    action='delete'
    removes the file with the specified PID. Note: in this version the delete action does not delete a file. Rather it sets the access value to 'deleted' and makes
    the digital object unavailable this way.

###access (profile, instruction and stagingfile elements)
Determines which dissemination policy to use when anonymous users want to view a level1, level2 or level3 derivative.
There are three default policies available:

    access='open'
    All derivatives are viewable to the world
    
    access='restricted'
    Level 1 is restricted; level 2 and 3 are viewable to the world
    
    access='closed' (default)
    no files can be downloaded or viewed

In the administration interface a CP administrator can add new policies and thus determine the view levels.

Note:
master files are always closed.

###contentType (profile, instruction and stagingfile elements)
This is the mimetype. A value that identifies the nature of the file ( like "image/jpeg", "application/pdf , etc)

Note: make sure your mimetype is correct or else the derivative creation or rendering may not go quite according to plan.
See http://en.wikipedia.org/wiki/MIME
and some good examples of them at http://www.ltsw.se/knbase/internet/mime.htp

###md5 (stagingfile element)
A checksum. Used to determine if the transport from the CP to the staging area was in deed without flaw.

###location (stagingfile element)
The case sensitive path and filename of the digital object. The beginning of the path is the same as the home directory.
In the earlier potato example that file's location would be: /folderC/folderD/Potato.tiff

###pid (stagingfile element)
The persistant identifier without a base URL. For example: 12345/my-id, hdl:12345/my-id, ark:12345/my-id

Note:
When unable to provide a pid value, use the optional substitute **lid** stagingfile element. This must contain a system
(your local organization's system that is ) wide unique local identifier. For examplee a LIS number.

##The values you MAY set

###label (profile and instruction element)
A human readable bit of information. This will show up on the administration interface when viewing your instructions.

###autoGeneratePIDs (profile and instruction element)
Used in combination with the administration interface **autoCreate instruction** and **upload instruction** command.
The setting is usefull when the filename can substitute a pid or lid value. And in proofs if you want to see how a
valid instruction would look like.

    autoGeneratePIDs='none' (default)

    autoGeneratePIDs='uuid'
    The system will provide an empty PID element with a seemingly random string.

    autoGeneratePIDs='filename2pid'
    Will substitute an empty PID element with the filename ( without suffix ).

    autoGeneratePIDs='filename2lid'
    Will substitute an empty lid element with the filename ( without suffix ).

    autoGeneratePIDs='lid'
    Will ignore an empty PID setting during auto creation and validation

###autoIngestValidInstruction (profile and instruction element)
When set to 'true' this will start a *file ingest*. But only if the instruction that was uploaded or automatically
 generated is valid. A file ingest will put the files into the object repository.

###resolverBaseUrl (profile and instruction element)
The resolver (a website that will redirect the user with the supplied PID to another url) of the PIDs you supplied in
the stagingfile elements. If the PID is attached to the resolverBaseUrl we ought to have a complete, workable url. This
is used at the dissemination end to create a link for the enduser.

##XML processing instruction schema
The content provider can create instructions and upload it to the staging area's main corresponding folders. This can
be done manually using the administration interface and ftp clients; or with automated procedures.

When you create your own XML processing instruction, then this schema is helpfull:
https://github.com/IISH/object-repository/blob/master/servicenodes/core/src/main/resource/instruction.v1.0.xsd

Note: in the schema you find elements and attributes marked in their annotation with the term 'system'.
These can be ignored if you make a custom instruction.

##Can I change the XML processing instruction after I uploaded it ?
Yes. A CP administrator can see all current instructions at the **instruction** tab. There the instructions and
 stagingfile elements can be tweaked. This feature is locked after ingest.

##Why does the instruction disappear after I uploaded it ?
Each XML processing instruction is read into a database. After that it will be physically removed from the staging area.
You can reupload the instruction any time you want. Or download it from the database back to your local PC or the staging area
main folder using the administration panel.

Note: when you download a XML processing instruction, it may have added validation error messages.

##When is the XML processing instruction "processed" ?
First, when a CP user ftps an XML processing instruction in a main folder. The staging area will detect a complete
instruction.xml document and insert it into a database to enhance viewing pleasure.

If you had set the element:

    autoIngestValidInstruction=true

And the instruction is valid, the ingest of your files will commence.

If you do not use the autoIngestValidInstruction setting, then it needs a manual command. From the administration
interface select **Ingest instruction** to start.

Note: the option **Ingest instruction** will not appear until the instruction is considered valid.

##Should I automatically create an XML processing instruction?
It can be usefull when you make your own custom instructions. You can use this feature to experiment to see how a
valid instruction looks like.

Should your PID or LID values be identical to the filenames, you could produce an instruction that is usuable for
ingest.

## How do I know my instruction is valid ?
An automatic validation procedure will run the moment you autocreate an instruction or ftp one into the main folder.
The results of the validation will show up in the CP administration interface per instruction and per file.
In addition, downloading the file via the CP administration interface will give you the XML instruction with **error** elements.

After you correct the issue, you can reupload the XML processing instruction and files.

## How do I monitor process ?
After an ingest command ( manual or automatic ) a progress bar will appear per instruction and per file. This gives the current
status of the ingest procedures.

##Two examples of an XML processing instruction

###Example with pid elements
Lets us assume a folder structure of one main folder with five files

    home-directory: /
        /folderA
        /folderA/Apple.tiff
        /folderA/Pear.tiff
        /folderA/Banana.tiff
        /folderA/Potato.png
        /folderA/SecretRecipe.tiff

And to stretch the borders of our imagination a little further, suppose the CP administrator has set the following default
settings in the profile via the administration interface:

    action=upsert
    access=closed
    contentType=image/tiff

Now we can mostly fall back on the defaults the profile provides, but we do want to add some exceptions to the
profile rules by adding new values the XML processing instruction:

1. Notice the **contentType** for the porato ? It is not a tiff but an **image/png**.
2. And we feel the **access** of the policy is too strict. Lets set it all to **open**.
3. And lets set the **access** to SecretRecipe.tiff to **restricted**.

The processing instruction would then could look like this:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <instruction xmlns="http://objectrepository.org/instruction/1.0/" access="open"> <!-- access added here -->
        <stagingfile>
            <pid>12345/a</pid>
            <location>/folderA/Apple.tiff</location>
            <md5>cf3ee1101d19328145e94da1cade45bd</md5>
        </stagingfile>
        <stagingfile>
            <pid>12345/b</pid>
            <location>/folderA/Pear.tiff</location>
            <md5>f57eb0e65d11a5a73eab9d5dddf1e528</md5>
        </stagingfile>
        <stagingfile>
            <pid>12345/c</pid>
            <location>/folderA/Banana.tiff</location>
            <md5>c741cd201fafb15c5ec874ae16738671</md5>
        </stagingfile>
        <stagingfile>
            <contentType>image/png</contentType> <!-- contentType here -->
            <pid>12345/d</pid>
            <location>/folderA/Potato.png</location>
            <md5>c741cd201fafb15c5ec874ae16738671</md5>
        </stagingfile>
        <stagingfile>
            <access>restricted</access> <!-- access element here -->
            <pid>12345/e</pid>
            <location>/folderA/SecretRecipe.tiff</location>
            <md5>c741cd201fafb15c5ec874ae16738671</md5>
        </stagingfile>
    </instruction>

###Example with lid elements
As a second example, lets suggest you are a content provider that can not supply persistant identifiers but only supply
local identifiers. Lets also assume that you made things easy and the local identifiers can be derived from the filenames
but without the extension.

Note: this example would only work in the object repository when it can fall back on a Handle System compatible PID
webservice for which a naming authority is required. In the example we assume this so and the authority is 12345.

Lets say these are the files in the staging area:

    home-directory: /
        /folderA
        /folderA/Apple.tiff
        /folderA/Pear.tiff
        /folderA/Banana.tiff
        /folderA/Potato.tiff
        /folderA/FreeRecipe.tiff

And we have set our profile defaults via the administration interface thus:

    action=upsert
    access=closed
    contentType=image/tiff
    autoGeneratePIDs='filename2lid' # This does the trick

An **automated instruction creation** or **instruction upload** via the administration interface of an instruction would be equal had you created this:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <instruction xmlns="http://objectrepository.org/instruction/1.0/">
        <stagingfile>
            <lid>12345/Apple</lid>
            <location>/folderA/Apple.tiff</location>
            <md5>cf3ee1101d19328145e94da1cade45bd</md5>
        </stagingfile>
        <stagingfile>
            <lid>12345/Pear</lid>
            <location>/folderA/Pear.tiff</location>
            <md5>f57eb0e65d11a5a73eab9d5dddf1e528</md5>
        </stagingfile>
        <stagingfile>
            <lid>12345/Banana</lid>
            <location>/folderA/Banana.tiff</location>
            <md5>c741cd201fafb15c5ec874ae16738671</md5>
        </stagingfile>
        <stagingfile>
            <lid>12345/Potato</lid>
            <location>/folderA/Potato.tiff</location>
            <md5>c741cd201fafb15c5ec874ae16738671</md5>
        </stagingfile>
        <stagingfile>
            <lid>12345/FreeRecipe</lid>
            <location>/folderA/FreeRecipe.tiff</location>
            <md5>d241cd201fafb15c5ec874ae167386ee</md5>
        </stagingfile>
    </instruction>
