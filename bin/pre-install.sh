

# deploy rosa cluster and wait 40min
rosa create account-roles --mode auto --yes
rosa create ocm-role --mode auto --admin --yes
rosa create user-role --mode auto --yes

# rosa create cluster --cluster-name demo-health --sts --mode auto --yes
############################################
## Install ROSA Cluster OCM UI            ##
## Configurate IdP, in my case use GitHub ##
############################################

# Added admin role to my github user
# ocm create user pkstaz --cluster=health-demo --group=cluster-admins 

