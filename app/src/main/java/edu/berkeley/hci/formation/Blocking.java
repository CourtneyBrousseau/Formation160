package edu.berkeley.hci.formation;

import android.media.AudioManager;
import android.media.SoundPool;

import java.util.ArrayList;

/**
 * Created by daphnenhuch on 4/6/18.
 */

public class Blocking {
    public ArrayList<Block> blocks;
    public SoundPool song;
    public AudioManager audioManager;

    Blocking() {
        this.blocks = new ArrayList<Block>();
    }

    Blocking(ArrayList<Block> blocks) {
        this.blocks = blocks;
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public void addBlock(Block block, Integer index) {
        blocks.add(index, block);
    }

    public void setBlock(Block block, Integer index) {
        blocks.set(index, block);
    }

    public ArrayList<Block> getBlocks() {
        return this.blocks;
    }

    public void removeBlock(Integer position) {
        int pos = position;
        int len = Math.round(this.blocks.get(pos).getEndTime() - this.blocks.get(pos).getStartTime());
        try {
            if (this.blocks.get(pos).getType().equals(Block.STATIC)) {

                if (pos > 0 && this.blocks.get(pos + 1).getType().equals(Block.TRANSITION) && this.blocks.get(pos - 1).getType().equals(Block.TRANSITION)) {
                    StaticBlock start = (StaticBlock) this.blocks.get(pos - 2);
                    StaticBlock end = (StaticBlock) this.blocks.get(pos + 2);
                    Block s = this.blocks.get(pos);
                    Block a = this.blocks.get(pos + 1);
                    Block b = this.blocks.remove(pos - 1);
                    this.blocks.remove(s);
                    this.blocks.remove(a);
                    this.blocks.remove(b);

                    TransitionBlock tb = new TransitionBlock(start);
                    tb.addEndBlock(end);
                    tb.makePaths();
                    for (int i = 0; i < 10; i++) {
                        end.incrStart();
                    }
                    addBlock(tb, pos - 1);
                    return;


                }
            }
            this.blocks.remove(pos);

        } catch (IndexOutOfBoundsException e) {
            this.blocks.remove(pos);
        }
        for (int i = position; i < blocks.size(); i ++) {
            Block b = blocks.get(i);
            for (int k = 0; k < len; k++) {
                b.decrEnd();
                b.decrStart();
            }
        }


    }

    public void incrTimeEnd(Block block) {
        int index = this.blocks.indexOf(block);
        this.blocks.get(index).incrEnd();
        for (int i = index + 1; i < this.blocks.size(); i++) {
            this.blocks.get(i).incrEnd();
            this.blocks.get(i).incrStart();
        }
    }

    public void incrTimeStart(Block block) {
        int index = this.blocks.indexOf(block);
        if (index > 0) {
            this.blocks.get(index - 1).incrEnd();
        }

        for (int i = index; i < this.blocks.size(); i++) {
            if (Math.round(this.blocks.get(i).getEndTime()) > this.blocks.get(i).getStartTime()) {

                this.blocks.get(i).incrStart();
               // this.blocks.get(i).incrEnd();
            }
        }
    }

    public void decrEnd(Block block) {
        int index = this.blocks.indexOf(block);
        this.blocks.get(index).decrEnd();

        for (int i = index + 1; i < this.blocks.size(); i++) {
            if (this.blocks.get(i).getEndTime() > this.blocks.get(i).getStartTime()) {
                this.blocks.get(i).decrStart();
                this.blocks.get(i).decrEnd();
            }
        }
    }

    public void decrStart(Block block) {
        int index = blocks.indexOf(block);
        for (int i = 0; i < index; i++) {
            this.blocks.get(i).decrStart();
            this.blocks.get(i).decrEnd();

        }
        this.blocks.get(index).decrStart();

    }

    public Integer getBlockByUid(String uid) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).getUid().equals(uid)) {
                return i;

            }
        }
        return -1;
    }
}
