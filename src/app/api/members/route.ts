import { NextRequest, NextResponse } from 'next/server'
import { MongoClient } from 'mongodb'

// Initialize MongoDB client
const client = new MongoClient(process.env.MONGODB_URI || 'mongodb://localhost:27017')

export async function GET() {
  try {
    await client.connect()
    const db = client.db("KitchenSink_DB")
    const members = await db.collection('members').find({}).toArray()
    
    return NextResponse.json(members)
  } catch (error) {
    console.error('GET Error:', error)
    return NextResponse.json(
      { error: 'Failed to fetch members' },
      { status: 500 }
    )
  } finally {
    await client.close()
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    
    // Validate required fields
    if (!body.name || !body.email || !body.phoneNumber) {
      return NextResponse.json(
        { error: 'Missing required fields' },
        { status: 400 }
      )
    }

    await client.connect()
    const db = client.db("KitchenSink_DB")
    
    const result = await db.collection('members').insertOne({
      name: body.name,
      email: body.email,
      phoneNumber: body.phoneNumber,
      createdAt: new Date()
    })

    const newMember = await db.collection('members').findOne({ _id: result.insertedId })
    
    return NextResponse.json(newMember)
  } catch (error) {
    console.error('POST Error:', error)
    return NextResponse.json(
      { error: 'Failed to add member' },
      { status: 500 }
    )
  } finally {
    await client.close()
  }
}

