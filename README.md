#Release notes and milestones

##Current version
The object repository is a collection of services:

 - ftp upload of files to a staging area (temporary storage)
 - ingesting those files into persistent storage
 - image derivative generation of three levels: level 1 ( high quality ); level 2 ( screen quality ) and level 3 ( thumbnail\preview )
 - accepts custom made derivatives of any content type
 - persistent identifier creation by calling a PID webservice (binding handles via a PID webservice.)
 - dissemination of the resources
 - an administration interface plus account system to manage the above services and access policies

##Milestone developments

###Version 3 beta
As from September 2012 - pilot phase

*A. Mets production*
Will have a service to generate XML Mets documents( profile 3 and 5; logical map ).
Descriptive logical relations are often expressed in the flat CSV documents a digital scanning factory delivers
to a CP ( when it mass produces high quality imagery ). This  will be the basis for Mets document production.

The Mets document that is fabricated this way can be downloaded via the dissemination API.

Note: Mets can also be producted by a content provider and offered for ingest.

*B. Rendering of Mets*
Ingested Mets will be used to:
 - create a PDF by the derivative service
 - extend it's given access policy ( open, restricted and closed ) to all master and derivative
material.
 - Display the logical relationships of the files at the dissemination API

*C. Video and audio derivative*
As with images, lower level copies and preview renderings will be produced.

*E. Upload social sites*
YouTube is the primary upload target

###Version 3 alpha###
As from December 2013
Ready for production

###Version 4###
Integrity validation of stored material. This involves random checksum calculations on files.
 When file degradation is detected reparations are performed.
