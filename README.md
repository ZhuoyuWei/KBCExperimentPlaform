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

And in the father class, EmbeddingModel, I have implemented L1-ball, L2-ball, SGD learning process, updating gradients. If you comply with the framework of Embedding Model, you can easy implement your own embedding-based methods just by rewrite CalculateGradient method. And if you want to run them, follow these command:

java wzy.main.AdjustParameter train_valid_test_data_dir init_embedding_filepath path_info_filepath

java wzy.main.LinkPrediction train_valid_test_data_dir init_embedding_filepath path_info_filepath

And the filenames in the train_valid_test_data_dir should be exp_train.txt, exp_valid.txt and exp_test.txt, or you can change them in the codes. I have add both WN18 and FB15K datasets in this project, but if you want to run the method in aaai paper, you should run TransE first and get the KB embedding, and our TransE implement can be used for it.

Tips:
a) In this project, System.err is used to print instant information, which is for monitoring the processing or schedule of the running of algorithm. On other hand, System.out is used to print results or others you want to remain, amd usually it should be redirected to file stream.

For Wn18 dataset, to make experiments for completing paths/formulas:

Without paths:

0.8042	0.1873	36.1082	0.9324	0.7517	21.9038

Random Walk Exactly:

0.8112	0.2024	35.8848	0.9370	0.774	22.0374

Random Attention, only by relation but not entity:

0.8077	0.2038	35.8445	0.9335	0.782	21.8128	

把Attention用作weight，如果不再用原来的weight，则结果为：0.715   0.1634  55.6153 0.8055  0.538   41.3667

如果继续乘以原来的weight，则结果为： 0.7491  0.1617  40.1713 0.8486  0.5922  25.8979

以上是10轮的训练结果，看看加大轮数会不会有提升
