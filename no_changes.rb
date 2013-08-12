all_no_change_commits = []

File.open('ChangeAnalyzer.log', "r").each_line do |line|
	if line =~ /WARNING: No changes distilled for file (\d+) at commit_id (\d+) from previous commit id (\d+)/ 
		all_no_change_commits << {:file_id=>$1, :commit_id=>$2, :pre_commit_id=>$3}
	end
end

length = all_no_change_commits.length
puts length
20.times do
	index = rand()*length
	puts index
	puts all_no_change_commits[index]
end
