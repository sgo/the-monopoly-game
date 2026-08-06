# About AI

So AI has been coming up. People have been impressed with what it can do.
Some even hope to put us all out of a job and make up the difference in profits...
Personally, I've not been too worried about that.

## Expectations vs Reality

I have the impression that a lot of people expect AI to be a smarter more competent human.
One that makes little to no mistakes.

And then we get disappointed.
There's even a name for that these days.
We get AI slop.
It makes me think of the uncanny valley.

The reality is that AI may be more knowledgeable. But it still has a non-perfect grasp of the knowledge it has. And it, like humans, will make assertions based on that dubious understanding.
It's just that it will, like humans, make them with confidence.

And then someone spots all the mistakes.
Is it fair to begrudge AI those mistakes?
As it turns out AI has a larger short- and long-term memory. In that sense it is better than a human.

But it has neither experience or wisdom.
The mistakes it makes can be very dumb.
Is that a problem though?

## We know how to fix our mistakes.

Anyone who's ever written or used software knows about bugs.
Humans make mistakes.

We also know how to catch our mistakes early.
This has been a hotly debated topic for a while before it died down.
The issue has been settled for a while, though adoption is not there yet.

We fix our human mistakes with a variety of tools and processes:
- Test Driven Development
- Behavior Driven Development or Acceptance Tests Driven Development or Specification by Example
- Static Code Analysis
- Mutation Testing
- Property Testing
- Quality Assurance (not so manual)

### The problem with motivation

I mentioned adoption of the techniques listed above has not fully happened yet.

The problem is humans struggle to find motivation to spend the effort required to apply all of these.
For example, who doesn't roll their eyes at Sonar gates blocking their submissions? I know I do. And then I haven't even tried to talk about Test Driven Development. Most teams still do something called Test After Development instead.

### AI doesn't complain

This project has been built by AI using these techniques.
And interestingly, in a multi agent setup where every agent has a focussed role it allows them to
detect their own mistakes and even fix them.

Is this surprising?
When AI are so similar to humans as fallible entities?

I feel like this is a breakthrough.
The question now is really just the energy and water costs required to run these things.

### AI doesn't even have to be the smartest

This works so well that we don't even need to flagship models.

I started out running Opus but as i'd quickly bump into both the 5 hour and weekly allowances I had to look for alternatives. So, I ended up combining claude with Codex. Eventually settling on...

- Specifier: Sonnet
- Coder: GPT-5.6 Luna
- Refactorer: Sonnet
- Architect: GPT-5.6 Terra

I also used DeepSeek V4 Flash for a moment and it was working fine.

## Should we be worried about our jobs?

I'm not sure.

This experiment has me basically managing, more or less successfully, a software development team consisting of AI agents. So those people could lose their jobs in time?

I'm not sure the cost-benefit balance is there yet.

I would also point out that being the eh... manager or tech lead of this team of AI agents is real work.
It's exhausting. Fun, for me at least. But exhausting.
