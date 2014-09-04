Small example I used to investigate okhttp performance during maven build.

The test was performed on a macbook connected using 5GHz wireless to a home router. The router
was connected to the Internet using 28/1 Mbps cable. Empirically, this connection is able to
sustain ~3.2MB/s on large HTTP downloads. 

The test uses artifact download log captured during build of maven master as of 2014-09-03.
When using empty local repository, the build took ~01:44. With populated local repository, 
the same build took ~0:33. This means that download of all artifacts (and their checksums)
took about ~70 seconds during this test build.

SequentialMain downloads all artifacts and their checksums one by one.

AsyncMain downloads all artifacts and their checksums using Okhttp built-in dispatcher

