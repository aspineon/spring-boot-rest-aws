## Summary
. This is a sample spring boot rest application.  
. The purpose of this application is to test its deployment on aws and to see the feasibility to using the existing domain by re-routing the domain requests to this application on aws.  
. This application is deployed as a tomcat application using aws elasticbeanstalk.    
. war file for this generated using maven.  
. Further details, as to how route requests for your domain to this app on aws and how to make the request secure are all given below.  
. This is an real-time bidding application on aws: https://www.slideshare.net/AmazonWebServices/app402?qid=bb0a7b0e-401f-4591-b259-1e35dc23b72a&v=&b=&from_search=24  
. This is another example: https://www.slideshare.net/AmazonWebServices/dvo312-sony-building-atscale-services-with-aws-elastic-beanstalk?next_slideshow=1  

## For routing all requests to your domain to aws elasticbeanstalk env:
Source: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/routing-to-beanstalk-environment.html
. I have followed everything that is mentioned in the link above.
### Summary is as follows:
. created hosted zone for the domain.  
. create a record set and assign it to the elastic beanstalk instance.  
. lower the ttl.  
. fetch the name server details from the hosted zone.  
. go to bigrock (which is where your registered your domain with) and modify the nameservers there (which today contains hostgator nameservers) with the aws ones.
. for these changes to take effect in might take anywhere between 2 hrs to 72 hrs (mine happened in 90 mins).  
. once this takes effect then officially your route 53 will become your dns service provider.  
 

## For making the connection to aws elasticbeanstalk env secure (https) by installing ssl certs:
Source: https://medium.com/@mitchelnijdam/letsencrypt-ssl-certificate-on-aws-eb-ec2-instance-776d3f60ee6c
### Connecting to EC2 using SSH
. In AWS, go to EC2 > Network & Security > Key pairs and generate a new keypair for your EC2 instance. Don’t forget to save the keys.  
. Make sure SSH is configured for your instance in AWS, if not go to EC2 instances and select the security group attached to this instance and click on inbound and add SSH so that port 22 is allowed on this instance.    
. install awsebcli: pip install awsebcli --upgrade --user  
. run the command eb ssh -- setup and follow the instructions. Make sure to select the correct instance where your beanstalk environment is running on.  
. Now running eb ssh should automatically connect to the instance using your private key.  
. For the above step to work, you need to put your private key (i.e. the keypair that is generated above for the ec2 instance) has to be put inside a .ssh folder in your user home directory on your local machine.  

### Setting up SSL certificates and server configuration
. SSH into your EC2 instance  
. cd /opt/  
. Did a cool trick to download certbot from GitHub: sudo wget https://github.com/certbot/certbot/archive/master.zip  
. unzip using: sudo unzip master.zip -d certbot  
. personally I liked having the certbot files in /opt/certbot/ so I copied them and got rid of the certbot-master directory  
. Run the certbot auto setup script: certbot/letsencrypt-auto --debug  
. So during the letsencrypt-auto setup I chose to redirect http to https (See end of this post for output of the setup). I assumed everything would work by now but when trying my URL in a browser showed that the redirect was working, but the site became unreachable. There were 2 last steps I had to take, which took most of the time to figure out.  

### Enable HTTPS on AWS Security Groups
. In AWS go to EC2 > Network & Security > Security Groups  
. Choose the security group that is attached to your instance or create a new security group and attach it later to your instance.  
. For Inbound choose edit and add HTTPS  
. After this, Chrome still couldn’t reach the site but now threw a new error: ERR_SSL_PROTOCOL_ERROR. Also https://www.ssllabs.com/ssltest/analyze.html?d=yourdomain.com told me “No secure protocols supported”.  

### Configure Apache httpd
. The final step in this whole process was a tricky one since I didn’t have any knowledge about Apache server configuration. The problem was there was no ssl configuration for port 443.  
. So the main Apache Server config file, for me placed in /etc/httpd/conf/httpd.conf had a part saying:
<IfModule mod_ssl.c>
Listen 443
</IfModule>
. Which means there is no further SSLCertificate configuration applied to port 443. So I modified these lines:
<IfModule mod_ssl.c>
Listen 443
IncludeOptional conf.d/*.conf
IncludeOptional conf.d/elasticbeanstalk/*.conf
</IfModule>
. and restarted the apache server: sudo service httpd restart  
. In this particular file /etc/httpd/conf.d/elasticbeanstalk/00_application-le-ssl.conf contained the configuration for the ssl certificates (created by the letsencrypt-auto setup).  
. after this you can verify, easiest way to verify by doing a cURL.

## Note
. If you terminate your environment in he aws console then the associated certs are removed and you need to install them again.  
. These certs expire in 90 days and as far as I know they don't get auto-renewed so you have to renew them again.

## The flow, how it works:  
. When you hit your domain url, first the hit goes to your bigrock which is where this domain is registered w/.  
. The nameservers that are configured w/ bigrock are looked-up and the control gets transferred to the nameserver location i.e. aws.  
. Route53 of aws comes into picture and then it looks up the hosted zone and its associated recordset and transfers the control to the attached instance, in this case, a elasticbeanstalk instance.  
. If it is a https request and if the https port is allowed in the inbound rule set for the ec2 instance then it is allowed and the certs are validated and request permitted.        

## Biling Details so far
. On 16th Apr 2019, 1400 hrs:  
. Put, Copy, Post, Lists requests: 1036 - 51.8  
. On 17th Apr 2019, 2000 hrs:   
. Put, Copy, Post, List req: 1078 - 53.9% : the app was up allthe time  
. But, Route53 charges was 0.59 USD i.e. 42 INR coz aws charges 0.50 USD per hostedzone for the first 25 hosted zones, so this should be a one time charge.  
. Keeping the aws running, will check tomorrow again.  
. Made one query to the url, will see if it changes anything tomorrow.  
. On 18th Apr, 2019:  
. Put, Copy, Post, List req: 1078 - 53.9% : app still up  
. Total bill amt still at 42 INR 
. On 19th Apr, 2019 at 2000 hrs:
. Put, Copy, Post, List req: 1078 - 53.9% : the app is still up
. Total billamt still at 42 INR  
