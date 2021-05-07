
if (!require('remotes')) install.packages('remotes', repos = "http://cran.us.r-project.org")
library(remotes)
if (!require('LogicOpt')) install_version("LogicOpt", "1.0.0", repos = "http://cran.us.r-project.org")
library(LogicOpt)

args <- commandArgs(trailingOnly=TRUE)
csv_file_path <- args[1]
output_file_path <- args[2]
num_input <- as.integer(args[3])
num_output <- as.integer(args[4])

policy <- read.csv(csv_file_path)
tte <- logicopt(policy,num_input,num_output,mode="espresso")
res <- tt2eqn(tte[[1]],num_input,num_output,QCA=FALSE)
write(res, output_file_path, sep="\n")
