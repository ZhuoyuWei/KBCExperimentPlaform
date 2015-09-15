# KBCExperimentPlaform
Knowledge base completion experiment platform. Any one can change experiment data set (must KB like), or implement your own model (especially Embedding-based model).

I have set up an inherit relationship from embedding model to all kinds of Trans models. So far, I have implemented following embedding methods:
TransE
TransH(it has different norm methods)
TransR
TransF
...
I also have implement our approach 'CPE' in my aaai2016 submission, which in wzy.model.TransEandPathModel. It is used to make experiments in this papaer.

There is two main class you should pay attention on: wzy.main.LinkPrediciton and wzy.main.AdjustParameter. The latter one is used to find best parameters for one specific parameters, and the former one runs one time when you have got the best parameters. In other word, AdjustParameter is a loop of LinkPrediction with different parameters.

And in the father class, EmbeddingModel, I have implemented L1-ball, L2-ball, SGD learning process, updating gradients. If you comply with the framework of Embedding Model, you can easy implement your own embedding-based methods just by rewrite CalculateGradient method.

Tips:
a) In this project, System.err is used to print instant information, which is for monitoring the processing or schedule of the running of algorithm. On other hand, System.out is used to print results or others you want to remain, amd usually it should be redirected to file stream.
