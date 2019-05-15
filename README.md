# WebCrawler
Scan Internet pages and save their titles. 

You can set number of threads-workers, which wait for new tasks in the task queue. 
A task is an URL. If a thread-worker gets a task, it goes to the page, saves its title, collects all links on the page, 
and adds this links as new tasks to the task queue. Save button export urls and titles of parsed pages into text file.

Restrictions:

Maximum crawling depth: if enabled, crawler won't go too deep in the Internet. 

Time limit: if enabled, crawler won't add tasks after the given time.

A Run/Stop button, which deactivate automatically if a restriction becomes valid or there is no task in the task queue.
