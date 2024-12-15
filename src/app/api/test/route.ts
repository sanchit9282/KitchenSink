import { NextResponse } from 'next/server'
import { MongoClient } from 'mongodb'

export async function GET() {
  const client = new MongoClient(process.env.MONGODB_URI || 'mongodb://localhost:27017')

  try {
    await client.connect()
    const db = client.db("KitchenSink_DB")
    const collection = db.collection('members')
    const count = await collection.countDocuments()

    return NextResponse.json({ message: "MongoDB connection successful", count })
  } catch (error) {
    console.error('MongoDB connection error:', error)
    return NextResponse.json({ error: "Failed to connect to MongoDB" }, { status: 500 })
  } finally {
    await client.close()
  }
}

