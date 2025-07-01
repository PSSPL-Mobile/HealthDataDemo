package com.psspl.healthdatademo.data.blockchain

class Blockchain {
    private val chain: MutableList<Block> = mutableListOf()

    init {
        addBlock(Block(0, System.currentTimeMillis(), "Genesis Block", "0"))
    }

    fun addBlock(newBlock: Block) {
        if (chain.isNotEmpty()) {
            newBlock.previousHash = chain.last().hash
        }
        newBlock.hash = Block.calculateHash(newBlock.index, newBlock.timestamp, newBlock.heartRateData, newBlock.previousHash)
        chain.add(newBlock)
    }

    fun getLatestBlock(): Block = chain.last()

    fun isChainValid(): Boolean {
        for (i in 1 until chain.size) {
            val currentBlock = chain[i]
            val previousBlock = chain[i - 1]
            if (currentBlock.hash != Block.calculateHash(currentBlock.index, currentBlock.timestamp, currentBlock.heartRateData, currentBlock.previousHash)) {
                return false
            }
            if (currentBlock.previousHash != previousBlock.hash) {
                return false
            }
        }
        return true
    }
}