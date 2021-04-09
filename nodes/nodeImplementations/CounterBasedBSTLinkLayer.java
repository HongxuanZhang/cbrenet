package projects.cbrenet.nodes.nodeImplementations;


/*
* 这里只负责Ego-Tree中结点的相连和移除，包括处理在StructureLayer的部分，但是不处理类似旋转位，发送位的问题！！*/

import projects.cbrenet.nodes.nodeImplementations.nodeHelper.LinkHelper;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public abstract class CounterBasedBSTLinkLayer extends CounterBasedBSTStructureLayer{

    LinkHelper linkHelper = LinkHelper.getInstance();

    // change link p l r, 这里就要考虑Auxiliary Node的问题了
    // 调用这个函数的时候，基本上也就是Delete过程或者Rotation过程了

    // may mainly used in rotation

    // delete 的时候如果用到AN，就调用这个method
    //int largeId, int egoTreeId, int trueId, int helpedId, EntryGetter entryGetter, Node helpedNode

    public boolean changeLeftChildTo(int largeId, int egoTreeId, int sendId){
        return this.linkHelper.changeLeftChildTo(largeId,egoTreeId,sendId, this.ID, this, this);
    }
    public boolean changeRightChildTo(int largeId, int egoTreeId, int sendId){
        return this.linkHelper.changeRightChildTo(largeId,egoTreeId,sendId, this.ID, this, this);
    }
    public boolean changeParentTo(int largeId, int egoTreeId, int sendId){
        return this.linkHelper.changeParentTo(largeId,egoTreeId,sendId, this.ID, this, this);
    }

    // add
    public boolean addLinkToLeftChild(int largeId, int egoTreeId, int sendId){
        return this.linkHelper.addLinkToLeftChild(largeId, egoTreeId, sendId, this.ID,this, this);
    }
    public boolean addLinkToRightChild(int largeId, int egoTreeId, int sendId){
        return this.linkHelper.addLinkToRightChild(largeId, egoTreeId, sendId, this.ID, this, this);
    }
    public boolean addLinkToParent(int largeId, int egoTreeId, int sendId){
        return this.linkHelper.addLinkToParent(largeId, egoTreeId, sendId, this.ID, this, this);
    }


    // remove link p l r

    // remove : delete corresponding link and set corresponding entry into -1;

    public boolean removeLinkToParent(int largeId){
        return this.linkHelper.removeLinkToParent(largeId, this.ID, this, this);
    }

    public boolean removeLinkToRightChild(int largeId){
        return this.linkHelper.removeLinkToRightChild(largeId, this.ID, this, this);
    }

    public boolean removeLinkToLeftChild(int largeId){
        return this.linkHelper.removeLinkToLeftChild(largeId, this.ID, this, this);
    }
}
